package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.ordemDePedido.exception.PagamentoNaoEncontradoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido.*;

/**
 * Aggregate root da ordem de pedido. Itens, pagamentos, notas fiscais e mensagens de incidente
 * pertencem à ordem e só são alterados através dela.
 *
 * Fluxo de status: AGUARDANDO_PAGAMENTO -> PAGO -> ENVIADO -> ENTREGUE
 *                  AGUARDANDO_PAGAMENTO | PAGO -> CANCELADO
 */
public class OrdemDePedido extends BaseDomain {

    private final String codigo;
    private LocalDate previsaoDeEntrega;
    private EnumStatusOrdemDePedido status;
    private final Usuario comprador;
    private final Usuario vendedor;
    private final List<ItemPedido> itens;
    private final List<Pagamento> pagamentos;
    private final List<MensagemIncidente> mensagensIncidente;
    private final List<NotaFiscal> notasFiscais;

    private OrdemDePedido(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String codigo, LocalDate previsaoDeEntrega, EnumStatusOrdemDePedido status, Usuario comprador, Usuario vendedor, List<ItemPedido> itens, List<Pagamento> pagamentos, List<MensagemIncidente> mensagensIncidente, List<NotaFiscal> notasFiscais) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.codigo = codigo;
        this.previsaoDeEntrega = previsaoDeEntrega;
        this.status = status;
        this.comprador = comprador;
        this.vendedor = vendedor;
        this.itens = copia(itens);
        this.pagamentos = copia(pagamentos);
        this.mensagensIncidente = copia(mensagensIncidente);
        this.notasFiscais = copia(notasFiscais);
    }

    public static OrdemDePedido criar(String codigo, Usuario comprador, Usuario vendedor, List<ItemPedido> itens) {
        if (comprador == null || vendedor == null) {
            throw new IllegalArgumentException("Comprador e vendedor são obrigatórios");
        }
        if (comprador.getId().equals(vendedor.getId())) {
            throw new RegraDeNegocioException("O comprador não pode comprar os próprios anúncios");
        }
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("A ordem de pedido precisa de ao menos um item");
        }
        return new OrdemDePedido(null, LocalDateTime.now(), null, null, codigo, null, AGUARDANDO_PAGAMENTO, comprador, vendedor, itens, List.of(), List.of(), List.of());
    }

    public static OrdemDePedido restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String codigo, LocalDate previsaoDeEntrega, EnumStatusOrdemDePedido status, Usuario comprador, Usuario vendedor, List<ItemPedido> itens, List<Pagamento> pagamentos, List<MensagemIncidente> mensagensIncidente, List<NotaFiscal> notasFiscais) {
        return new OrdemDePedido(id, dataCadastro, dataAlteracao, dataExcluido, codigo, previsaoDeEntrega, status, comprador, vendedor, itens, pagamentos, mensagensIncidente, notasFiscais);
    }

    /******************************************* PERMISSÕES *******************************************/

    public boolean isComprador(UUID usuarioId) { return comprador.getId().equals(usuarioId); }

    public boolean isVendedor(UUID usuarioId) { return vendedor.getId().equals(usuarioId); }

    public boolean isParticipante(UUID usuarioId) { return isComprador(usuarioId) || isVendedor(usuarioId); }

    public void garantirParticipante(UUID usuarioId) {
        if (!isParticipante(usuarioId)) {
            throw new AcessoNegadoException("Usuário não participa desta ordem de pedido");
        }
    }

    private void garantirComprador(UUID usuarioId) {
        if (!isComprador(usuarioId)) {
            throw new AcessoNegadoException("Apenas o comprador pode executar esta operação");
        }
    }

    private void garantirVendedor(UUID usuarioId) {
        if (!isVendedor(usuarioId)) {
            throw new AcessoNegadoException("Apenas o vendedor pode executar esta operação");
        }
    }

    private void garantirStatus(EnumStatusOrdemDePedido... permitidos) {
        for (EnumStatusOrdemDePedido s : permitidos) {
            if (status == s) return;
        }
        throw new RegraDeNegocioException("Operação não permitida para ordem com status " + status);
    }

    /******************************************* REGRAS *******************************************/

    public void registrarPagamento(UUID usuarioId, Pagamento pagamento) {
        garantirComprador(usuarioId);
        garantirStatus(AGUARDANDO_PAGAMENTO);
        if (pagamento.getValor().compareTo(getValorRestante()) > 0) {
            throw new RegraDeNegocioException("Valor do pagamento (" + pagamento.getValor() + ") maior que o valor restante (" + getValorRestante() + ")");
        }
        pagamentos.add(pagamento);
        if (getValorRestante().compareTo(BigDecimal.ZERO) == 0) {
            status = PAGO;
        }
    }

    /**
     * O comprador exclui um pagamento registrado por engano. Só antes do envio: se a ordem já estava
     * PAGA, ela volta a aguardar pagamento.
     */
    public Pagamento removerPagamento(UUID usuarioId, UUID pagamentoId) {
        garantirComprador(usuarioId);
        if (status != AGUARDANDO_PAGAMENTO && status != PAGO) {
            throw new RegraDeNegocioException("Pagamentos só podem ser excluídos antes do envio do pedido (status atual: " + status + ")");
        }
        Pagamento pagamento = pagamentos.stream().filter(p -> p.getId() != null && p.getId().equals(pagamentoId)).findFirst()
                .orElseThrow(() -> new PagamentoNaoEncontradoException(pagamentoId));
        pagamentos.remove(pagamento);
        if (status == PAGO) {
            status = AGUARDANDO_PAGAMENTO;
        }
        return pagamento;
    }

    public void adicionarNotaFiscal(UUID usuarioId, NotaFiscal notaFiscal) {
        garantirVendedor(usuarioId);
        garantirStatus(PAGO, ENVIADO, ENTREGUE);
        notasFiscais.add(notaFiscal);
    }

    public void enviar(UUID usuarioId, LocalDate previsaoDeEntrega) {
        garantirVendedor(usuarioId);
        garantirStatus(PAGO);
        if (previsaoDeEntrega != null && previsaoDeEntrega.isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("A previsão de entrega não pode estar no passado");
        }
        this.previsaoDeEntrega = previsaoDeEntrega;
        this.status = ENVIADO;
    }

    public void confirmarEntrega(UUID usuarioId) {
        garantirComprador(usuarioId);
        garantirStatus(ENVIADO);
        this.status = ENTREGUE;
    }

    public void cancelar(UUID usuarioId) {
        garantirParticipante(usuarioId);
        garantirStatus(AGUARDANDO_PAGAMENTO, PAGO);
        this.status = CANCELADO;
    }

    public void registrarMensagemIncidente(UUID usuarioId, MensagemIncidente mensagem) {
        garantirParticipante(usuarioId);
        mensagensIncidente.add(mensagem);
    }

    /******************************************* VALORES *******************************************/

    public BigDecimal getValorTotal() {
        return itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorPago() {
        return pagamentos.stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorRestante() {
        return getValorTotal().subtract(getValorPago());
    }

    private static <T> List<T> copia(List<T> lista) {
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    /******************************************* GETTERS *******************************************/

    public String getCodigo() { return codigo; }
    public LocalDate getPrevisaoDeEntrega() { return previsaoDeEntrega; }
    public EnumStatusOrdemDePedido getStatus() { return status; }
    public Usuario getComprador() { return comprador; }
    public Usuario getVendedor() { return vendedor; }
    public List<ItemPedido> getItens() { return List.copyOf(itens); }
    public List<Pagamento> getPagamentos() { return List.copyOf(pagamentos); }
    public List<MensagemIncidente> getMensagensIncidente() { return List.copyOf(mensagensIncidente); }
    public List<NotaFiscal> getNotasFiscais() { return List.copyOf(notasFiscais); }
}
