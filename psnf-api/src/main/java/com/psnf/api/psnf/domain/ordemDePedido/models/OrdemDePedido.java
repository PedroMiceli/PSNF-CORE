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
 * Fluxo de status: AGUARDANDO_CONFIRMACAO -> ACEITO -> AGUARDANDO_PAGAMENTO -> PAGO -> ENVIADO -> ENTREGUE
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
    /** até quando comprador e vendedor já viram as mensagens de incidente (para destacar as novas) */
    private LocalDateTime incidentesLidosCompradorEm;
    /** detalhes da entrega informados pelo vendedor (descrição e código de envio); null se não houver */
    private Entrega entrega;
    /** negociação (chat) em que a ordem foi combinada; null se foi criada fora do chat */
    private UUID negociacaoId;
    /**
     * Última mudança que o outro lado precisa ver (status, pagamento, comprovante, NF, entrega, mensagem).
     * Usada pela consulta leve de novidades: a tela só recarrega a ordem quando este valor muda.
     */
    private LocalDateTime atualizadaEm;
    private LocalDateTime incidentesLidosVendedorEm;
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
        return new OrdemDePedido(null, LocalDateTime.now(), null, null, codigo, null, AGUARDANDO_CONFIRMACAO, comprador, vendedor, itens, List.of(), List.of(), List.of());
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

    /** O comprador conferiu produtos, quantidades e valores e aceita a ordem. */
    public void aceitar(UUID usuarioId) {
        tocar();
        garantirComprador(usuarioId);
        garantirStatus(AGUARDANDO_CONFIRMACAO);
        status = ACEITO;
    }

    /**
     * Renegociação: o comprador ainda não aceitou (achou caro, por exemplo) e o vendedor ajusta o valor
     * unitário de um ou mais itens. Só em AGUARDANDO_CONFIRMACAO: depois de aceita, a ordem fica como foi
     * combinada (e nesse status ainda não há formas de pagamento para desencontrar do novo total).
     * O comprador é avisado por uma mensagem automática em "Incidentes e mensagens".
     *
     * @param novos valor unitário novo por id do item; itens com o mesmo valor são ignorados
     * @return a mensagem registrada, com o antes e o depois
     */
    public MensagemIncidente alterarValores(UUID usuarioId, java.util.Map<UUID, BigDecimal> novos) {
        tocar();
        garantirVendedor(usuarioId);
        if (status != AGUARDANDO_CONFIRMACAO) {
            throw new RegraDeNegocioException("Os valores só podem ser alterados enquanto o comprador não aceitou a ordem");
        }
        BigDecimal totalAntes = getValorTotal();
        java.text.NumberFormat moeda = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.of("pt", "BR"));
        StringBuilder linhas = new StringBuilder();
        for (var e : novos.entrySet()) {
            ItemPedido item = itens.stream().filter(i -> e.getKey().equals(i.getId())).findFirst()
                    .orElseThrow(() -> new RegraDeNegocioException("O item " + e.getKey() + " não pertence a esta ordem"));
            BigDecimal novo = e.getValue() == null ? null : e.getValue().setScale(2, java.math.RoundingMode.HALF_UP);
            if (novo != null && novo.compareTo(item.getValorUnidade()) == 0) continue;
            BigDecimal antes = item.getValorUnidade();
            item.alterarValorUnidade(novo);
            linhas.append("\n• ").append(item.getTituloAnuncio())
                    .append(item.getVariacao() == null ? "" : " (" + item.getVariacao() + ")")
                    .append(": ").append(moeda.format(antes)).append(" → ").append(moeda.format(novo)).append(" por unidade");
        }
        if (linhas.isEmpty()) throw new RegraDeNegocioException("Nenhum valor foi alterado");
        MensagemIncidente aviso = MensagemIncidente.criar("Valores renegociados pelo vendedor:" + linhas
                + "\nNovo total: " + moeda.format(getValorTotal()) + " (antes " + moeda.format(totalAntes) + ")", null, usuarioId);
        mensagensIncidente.add(aviso);
        marcarIncidentesLidos(usuarioId); // o vendedor já sabe; o comprador recebe o alerta de mensagem nova
        return aviso;
    }

    /**
     * O VENDEDOR cadastra uma forma de pagamento combinada com o comprador (ex.: parte no PIX, parte no boleto),
     * com os dados para o comprador pagar. Quando as formas cobrem o valor total, a ordem passa a aguardar pagamento.
     */
    public void registrarPagamento(UUID usuarioId, Pagamento pagamento) {
        tocar();
        garantirVendedor(usuarioId);
        if (status == AGUARDANDO_CONFIRMACAO) {
            throw new RegraDeNegocioException("Aguarde o comprador aceitar a ordem para cadastrar as formas de pagamento");
        }
        garantirStatus(ACEITO);
        if (pagamento.getValor().compareTo(getValorRestante()) > 0) {
            throw new RegraDeNegocioException("Valor do pagamento (" + pagamento.getValor() + ") maior que o valor restante (" + getValorRestante() + ")");
        }
        pagamentos.add(pagamento);
        if (getValorRestante().compareTo(BigDecimal.ZERO) == 0) {
            status = AGUARDANDO_PAGAMENTO;
        }
    }

    /**
     * O vendedor exclui uma forma de pagamento cadastrada por engano, desde que o comprador ainda não tenha
     * anexado o comprovante. Se a ordem já aguardava pagamento, volta para ACEITO (falta valor a cadastrar).
     */
    public Pagamento removerPagamento(UUID usuarioId, UUID pagamentoId) {
        tocar();
        garantirVendedor(usuarioId);
        if (status != ACEITO && status != AGUARDANDO_PAGAMENTO) {
            throw new RegraDeNegocioException("Formas de pagamento só podem ser excluídas antes do pagamento (status atual: " + status + ")");
        }
        Pagamento pagamento = buscarPagamento(pagamentoId);
        if (pagamento.temComprovante()) {
            throw new RegraDeNegocioException("Este pagamento já tem comprovante anexado pelo comprador e não pode ser excluído");
        }
        pagamentos.remove(pagamento);
        status = ACEITO;
        return pagamento;
    }

    /**
     * O COMPRADOR anexa o comprovante de um pagamento (ou troca o que enviou). Quando todos os pagamentos
     * têm comprovante, a ordem fica PAGA e o vendedor pode despachar.
     */
    public void anexarComprovante(UUID usuarioId, UUID pagamentoId, String caminho) {
        tocar();
        garantirComprador(usuarioId);
        // também depois do envio: numa venda a prazo o comprador paga depois de receber
        if (status == AGUARDANDO_CONFIRMACAO || status == CANCELADO) {
            throw new RegraDeNegocioException("Não é possível anexar comprovantes com a ordem no status " + status);
        }
        buscarPagamento(pagamentoId).anexarComprovante(caminho);
        if (status == AGUARDANDO_PAGAMENTO && pagamentos.stream().allMatch(Pagamento::temComprovante)) {
            status = PAGO;
        }
    }

    private Pagamento buscarPagamento(UUID pagamentoId) {
        return pagamentos.stream().filter(p -> p.getId() != null && p.getId().equals(pagamentoId)).findFirst()
                .orElseThrow(() -> new PagamentoNaoEncontradoException(pagamentoId));
    }

    public void adicionarNotaFiscal(UUID usuarioId, NotaFiscal notaFiscal) {
        tocar();
        garantirVendedor(usuarioId);
        garantirStatus(AGUARDANDO_PAGAMENTO, PAGO, ENVIADO, ENTREGUE);
        notasFiscais.add(notaFiscal);
    }

    /**
     * O vendedor despacha o pedido. Normalmente depois do pagamento, mas também com pagamento pendente
     * (ex.: venda a prazo, o comprador paga depois de receber): a ordem segue e continua indicando
     * {@link #isPagamentoPendente()} até todos os comprovantes chegarem.
     */
    public void enviar(UUID usuarioId, LocalDate previsaoDeEntrega) {
        enviar(usuarioId, previsaoDeEntrega, null, null);
    }

    /** Despacha informando, se quiser, a descrição da entrega e o código de envio. */
    public void enviar(UUID usuarioId, LocalDate previsaoDeEntrega, String descricaoEntrega, String codigoRastreio) {
        enviar(usuarioId, previsaoDeEntrega, descricaoEntrega, codigoRastreio, null);
    }

    /** @param retiradaNoLocal null = mantém o que já estava combinado */
    public void enviar(UUID usuarioId, LocalDate previsaoDeEntrega, String descricaoEntrega, String codigoRastreio,
                       Boolean retiradaNoLocal) {
        tocar();
        garantirVendedor(usuarioId);
        if (status == AGUARDANDO_CONFIRMACAO || status == ACEITO) {
            throw new RegraDeNegocioException("Cadastre as formas de pagamento cobrindo o valor total antes de despachar o pedido");
        }
        garantirStatus(AGUARDANDO_PAGAMENTO, PAGO);
        if (previsaoDeEntrega != null && previsaoDeEntrega.isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("A previsão de entrega não pode estar no passado");
        }
        this.previsaoDeEntrega = previsaoDeEntrega;
        this.status = ENVIADO;
        if (descricaoEntrega != null || codigoRastreio != null || retiradaNoLocal != null) {
            // no despacho, campo não informado mantém o que já foi salvo em "Entrega" (não apaga instruções)
            definirEntrega(usuarioId,
                    descricaoEntrega != null ? descricaoEntrega : entrega == null ? null : entrega.getDescricao(),
                    codigoRastreio != null ? codigoRastreio : entrega == null ? null : entrega.getCodigoRastreio(),
                    retiradaNoLocal);
        }
    }

    public void confirmarEntrega(UUID usuarioId) {
        tocar();
        garantirComprador(usuarioId);
        garantirStatus(ENVIADO);
        this.status = ENTREGUE;
    }

    public void cancelar(UUID usuarioId) {
        tocar();
        garantirParticipante(usuarioId);
        garantirStatus(AGUARDANDO_CONFIRMACAO, ACEITO, AGUARDANDO_PAGAMENTO, PAGO);
        this.status = CANCELADO;
    }

    public void registrarMensagemIncidente(UUID usuarioId, MensagemIncidente mensagem) {
        tocar();
        garantirParticipante(usuarioId);
        mensagensIncidente.add(mensagem);
        marcarIncidentesLidos(usuarioId); // quem escreve já viu a conversa até aqui
    }

    /** Registra que a ordem mudou agora (ver {@link #atualizadaEm}). As validações vêm depois e, se falharem, nada é salvo. */
    private void tocar() {
        this.atualizadaEm = LocalDateTime.now();
    }

    public LocalDateTime getAtualizadaEm() { return atualizadaEm != null ? atualizadaEm : getDataCadastro(); }

    /** Usado pela persistência. */
    public void restaurarAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }

    /******************************************* ENTREGA *******************************************/

    /**
     * O vendedor informa ou corrige os detalhes da entrega (descrição e/ou código de envio), a partir do aceite
     * do comprador até a entrega. Com os dois campos vazios, a entrega é removida.
     */
    public void registrarEntrega(UUID usuarioId, String descricao, String codigoRastreio) {
        registrarEntrega(usuarioId, descricao, codigoRastreio, null);
    }

    /** @param retiradaNoLocal null = mantém o que já estava combinado */
    public void registrarEntrega(UUID usuarioId, String descricao, String codigoRastreio, Boolean retiradaNoLocal) {
        tocar();
        garantirVendedor(usuarioId);
        if (status == AGUARDANDO_CONFIRMACAO || status == CANCELADO) {
            throw new RegraDeNegocioException("Os dados da entrega só podem ser informados depois que o comprador aceitar a ordem");
        }
        definirEntrega(usuarioId, descricao, codigoRastreio, retiradaNoLocal);
    }

    /**
     * Quando a retirada no local muda (marcada ou desmarcada), o comprador recebe uma mensagem automática
     * em "Incidentes e mensagens", com o alerta de mensagem nova: o combinado pode mudar depois da compra.
     */
    private void definirEntrega(UUID vendedorId, String descricao, String codigoRastreio, Boolean retiradaNoLocal) {
        boolean antes = entrega != null && entrega.isRetiradaNoLocal();
        boolean depois = retiradaNoLocal == null ? antes : retiradaNoLocal;
        if (entrega == null) {
            entrega = Entrega.criar(descricao, codigoRastreio, depois);
        } else if (!entrega.atualizar(descricao, codigoRastreio, depois)) {
            entrega = null;
        }
        if (antes != depois) {
            mensagensIncidente.add(MensagemIncidente.criar(depois
                    ? "O vendedor indicou que este pedido deve ser RETIRADO NO LOCAL (no fornecedor), sem envio por "
                      + "transportadora, por isso não haverá código de rastreio. Combine aqui o endereço, o dia e o horário da retirada."
                    : "O vendedor alterou a entrega: o pedido NÃO será mais retirado no local e passa a ser enviado. "
                      + "O código de rastreio, se houver, aparece em \"Entrega\".", null, vendedorId));
            marcarIncidentesLidos(vendedorId);
        }
    }

    /** Vincula a ordem à negociação (chat) em que foi combinada. */
    public void vincularNegociacao(UUID negociacaoId) {
        this.negociacaoId = negociacaoId;
    }

    public UUID getNegociacaoId() { return negociacaoId; }

    /** Usado pela persistência. */
    public void restaurarEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public Entrega getEntrega() { return entrega; }

    /******************************************* LEITURA DOS INCIDENTES *******************************************/

    /** O usuário viu as mensagens de incidente: deixam de contar como novas para ele. */
    public void marcarIncidentesLidos(UUID usuarioId) {
        garantirParticipante(usuarioId);
        LocalDateTime agora = LocalDateTime.now();
        if (isComprador(usuarioId)) incidentesLidosCompradorEm = agora;
        else incidentesLidosVendedorEm = agora;
    }

    /** Mensagens de incidente escritas pelo outro lado que este usuário ainda não viu. */
    public int mensagensIncidenteNaoLidas(UUID usuarioId) {
        if (usuarioId == null || !isParticipante(usuarioId)) return 0;
        LocalDateTime lidoEm = isComprador(usuarioId) ? incidentesLidosCompradorEm : incidentesLidosVendedorEm;
        return (int) mensagensIncidente.stream()
                .filter(m -> !usuarioId.equals(m.getUsuarioCriador()))
                .filter(m -> lidoEm == null || m.getDataCadastro().isAfter(lidoEm))
                .count();
    }

    /** Usado pela persistência para restaurar quando cada lado leu as mensagens. */
    public void restaurarLeituraIncidentes(LocalDateTime compradorEm, LocalDateTime vendedorEm) {
        this.incidentesLidosCompradorEm = compradorEm;
        this.incidentesLidosVendedorEm = vendedorEm;
    }

    public LocalDateTime getIncidentesLidosCompradorEm() { return incidentesLidosCompradorEm; }
    public LocalDateTime getIncidentesLidosVendedorEm() { return incidentesLidosVendedorEm; }

    /******************************************* VALORES *******************************************/

    public BigDecimal getValorTotal() {
        return itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** soma das formas de pagamento cadastradas pelo vendedor */
    public BigDecimal getValorCadastrado() {
        return pagamentos.stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** soma dos pagamentos com comprovante anexado pelo comprador */
    public BigDecimal getValorPago() {
        return pagamentos.stream().filter(Pagamento::temComprovante).map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** ainda falta pagar (algum pagamento sem comprovante), mesmo que o pedido já tenha sido enviado */
    public boolean isPagamentoPendente() {
        return status != CANCELADO && getValorPago().compareTo(getValorTotal()) < 0;
    }

    /** valor que o vendedor ainda precisa distribuir em formas de pagamento */
    public BigDecimal getValorRestante() {
        return getValorTotal().subtract(getValorCadastrado());
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
