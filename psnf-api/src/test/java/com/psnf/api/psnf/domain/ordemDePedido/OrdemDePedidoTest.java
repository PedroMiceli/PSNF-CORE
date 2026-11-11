package com.psnf.api.psnf.domain.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.exception.PagamentoNaoEncontradoException;
import com.psnf.api.psnf.domain.ordemDePedido.models.ItemPedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.MensagemIncidente;
import com.psnf.api.psnf.domain.ordemDePedido.models.NotaFiscal;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.Pagamento;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDePedidoTest {

    private Usuario comprador;
    private Usuario vendedor;
    private OrdemDePedido ordem;

    private static Usuario usuario(String nome) {
        return Usuario.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null, nome, nome + "@x.com", "hash", null, null, null, List.of());
    }

    private static Pagamento pix(String valor) {
        return Pagamento.criar(EnumTipoPagamento.PIX, new BigDecimal(valor), null, null, null, null, "chave", null);
    }

    @BeforeEach
    void setUp() {
        comprador = usuario("comprador");
        vendedor = usuario("vendedor");
        ordem = OrdemDePedido.criar("PED-1", comprador, vendedor, List.of(
                ItemPedido.criar(UUID.randomUUID(), "Camiseta", "Azul", 2, new BigDecimal("30.00")),
                ItemPedido.criar(UUID.randomUUID(), "Boné", null, 1, new BigDecimal("40.00"))));
    }

    @Test
    void calculaValorTotal() {
        assertEquals(new BigDecimal("100.00"), ordem.getValorTotal());
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_CONFIRMACAO, ordem.getStatus());
    }

    @Test
    void naoPermiteComprarDeSiMesmo() {
        assertThrows(RegraDeNegocioException.class, () -> OrdemDePedido.criar("PED-2", comprador, comprador,
                List.of(ItemPedido.criar(UUID.randomUUID(), "X", null, 1, BigDecimal.ONE))));
    }

    /** aceita a ordem e cadastra a forma de pagamento (já salva, com id) cobrindo o total */
    private Pagamento aceitarECadastrarTudo() {
        ordem.aceitar(comprador.getId());
        Pagamento p = pixSalvo("100.00");
        ordem.registrarPagamento(vendedor.getId(), p);
        return p;
    }

    @Test
    void comecaAguardandoConfirmacaoDoComprador() {
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_CONFIRMACAO, ordem.getStatus());
        // antes do aceite, o vendedor não cadastra pagamentos
        assertThrows(RegraDeNegocioException.class, () -> ordem.registrarPagamento(vendedor.getId(), pix("10.00")));
        assertThrows(AcessoNegadoException.class, () -> ordem.aceitar(vendedor.getId()));
        ordem.aceitar(comprador.getId());
        assertEquals(EnumStatusOrdemDePedido.ACEITO, ordem.getStatus());
        assertThrows(RegraDeNegocioException.class, () -> ordem.aceitar(comprador.getId()));
    }

    @Test
    void fluxoCompletoAteEntrega() {
        ordem.aceitar(comprador.getId());
        Pagamento pix = pixSalvo("60.00");
        Pagamento boleto = Pagamento.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null, EnumTipoPagamento.BOLETO,
                new BigDecimal("40.00"), null, null, null, null, null, "34191.79001", LocalDate.now().plusDays(10));
        ordem.registrarPagamento(vendedor.getId(), pix);
        assertEquals(EnumStatusOrdemDePedido.ACEITO, ordem.getStatus()); // ainda falta cadastrar 40
        ordem.registrarPagamento(vendedor.getId(), boleto);
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_PAGAMENTO, ordem.getStatus());
        assertEquals(BigDecimal.ZERO.compareTo(ordem.getValorRestante()), 0);
        assertEquals(BigDecimal.ZERO.compareTo(ordem.getValorPago()), 0);

        ordem.anexarComprovante(comprador.getId(), pix.getId(), "/arquivos/comprovante-pix.pdf");
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_PAGAMENTO, ordem.getStatus()); // falta o do boleto
        assertEquals(new BigDecimal("60.00"), ordem.getValorPago());
        ordem.anexarComprovante(comprador.getId(), boleto.getId(), "/arquivos/comprovante-boleto.png");
        assertEquals(EnumStatusOrdemDePedido.PAGO, ordem.getStatus());

        ordem.adicionarNotaFiscal(vendedor.getId(), NotaFiscal.criar("nf.pdf"));
        ordem.enviar(vendedor.getId(), LocalDate.now().plusDays(3));
        assertEquals(EnumStatusOrdemDePedido.ENVIADO, ordem.getStatus());
        ordem.confirmarEntrega(comprador.getId());
        assertEquals(EnumStatusOrdemDePedido.ENTREGUE, ordem.getStatus());
    }

    @Test
    void naoAceitaPagamentoMaiorQueRestante() {
        ordem.aceitar(comprador.getId());
        assertThrows(RegraDeNegocioException.class, () -> ordem.registrarPagamento(vendedor.getId(), pix("100.01")));
    }

    @Test
    void vendedorCadastraPagamentoECompradorAnexaComprovante() {
        ordem.aceitar(comprador.getId());
        assertThrows(AcessoNegadoException.class, () -> ordem.registrarPagamento(comprador.getId(), pix("10")));
        Pagamento p = aceitarDepois();
        assertThrows(AcessoNegadoException.class, () -> ordem.anexarComprovante(vendedor.getId(), p.getId(), "/arquivos/x.pdf"));
        assertThrows(IllegalArgumentException.class, () -> ordem.anexarComprovante(comprador.getId(), p.getId(), " "));
        assertThrows(AcessoNegadoException.class, () -> ordem.enviar(comprador.getId(), null));
    }

    private Pagamento aceitarDepois() {
        Pagamento p = pixSalvo("100.00");
        ordem.registrarPagamento(vendedor.getId(), p);
        return p;
    }

    @Test
    void naoEnviaAntesDeCadastrarOsPagamentos() {
        assertThrows(RegraDeNegocioException.class, () -> ordem.enviar(vendedor.getId(), null));
        ordem.aceitar(comprador.getId());
        ordem.registrarPagamento(vendedor.getId(), pixSalvo("40.00")); // falta cadastrar 60
        assertThrows(RegraDeNegocioException.class, () -> ordem.enviar(vendedor.getId(), null));
    }

    @Test
    void vendaAPrazo_enviaComPagamentoPendenteEComprovanteChegaDepois() {
        Pagamento p = aceitarECadastrarTudo();
        assertTrue(ordem.isPagamentoPendente());
        ordem.adicionarNotaFiscal(vendedor.getId(), NotaFiscal.criar("nf.pdf"));
        ordem.enviar(vendedor.getId(), null); // sem comprovante
        assertEquals(EnumStatusOrdemDePedido.ENVIADO, ordem.getStatus());
        assertTrue(ordem.isPagamentoPendente());

        ordem.confirmarEntrega(comprador.getId());
        assertEquals(EnumStatusOrdemDePedido.ENTREGUE, ordem.getStatus());
        assertTrue(ordem.isPagamentoPendente());
        // o vendedor não exclui mais a forma de pagamento depois do envio
        assertThrows(RegraDeNegocioException.class, () -> ordem.removerPagamento(vendedor.getId(), p.getId()));

        ordem.anexarComprovante(comprador.getId(), p.getId(), "/arquivos/pago-depois.pdf");
        assertEquals(EnumStatusOrdemDePedido.ENTREGUE, ordem.getStatus()); // o status não volta
        assertFalse(ordem.isPagamentoPendente());
    }

    @Test
    void ordemCanceladaNaoFicaComPagamentoPendente() {
        ordem.cancelar(comprador.getId());
        assertFalse(ordem.isPagamentoPendente());
    }

    @Test
    void naoCancelaDepoisDeEnviada() {
        Pagamento p = aceitarECadastrarTudo();
        ordem.anexarComprovante(comprador.getId(), p.getId(), "/arquivos/c.pdf");
        ordem.enviar(vendedor.getId(), null);
        assertThrows(RegraDeNegocioException.class, () -> ordem.cancelar(comprador.getId()));
    }

    @Test
    void compradorPodeCancelarEmVezDeAceitar() {
        ordem.cancelar(comprador.getId());
        assertEquals(EnumStatusOrdemDePedido.CANCELADO, ordem.getStatus());
    }

    @Test
    void terceiroNaoEnviaMensagem() {
        UUID terceiro = UUID.randomUUID();
        assertThrows(AcessoNegadoException.class,
                () -> ordem.registrarMensagemIncidente(terceiro, MensagemIncidente.criar("oi", null, terceiro)));
    }

    @Test
    void pagamentoValidaCamposPorTipo() {
        assertThrows(IllegalArgumentException.class,
                () -> Pagamento.criar(EnumTipoPagamento.TRANSFERENCIA, BigDecimal.TEN, null, "001", "123", null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> Pagamento.criar(EnumTipoPagamento.BOLETO, BigDecimal.TEN, null, null, null, null, null, " "));
    }

    private static Pagamento pixSalvo(String valor) {
        return Pagamento.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null, EnumTipoPagamento.PIX, new BigDecimal(valor),
                null, null, null, null, "chave", null);
    }

    @Test
    void vendedorExcluiFormaDePagamentoSemComprovante() {
        Pagamento errado = aceitarECadastrarTudo();
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_PAGAMENTO, ordem.getStatus());
        assertThrows(AcessoNegadoException.class, () -> ordem.removerPagamento(comprador.getId(), errado.getId()));
        ordem.removerPagamento(vendedor.getId(), errado.getId());
        assertTrue(ordem.getPagamentos().isEmpty());
        assertEquals(new BigDecimal("100.00"), ordem.getValorRestante());
        assertEquals(EnumStatusOrdemDePedido.ACEITO, ordem.getStatus()); // volta a cadastrar
        assertThrows(PagamentoNaoEncontradoException.class, () -> ordem.removerPagamento(vendedor.getId(), errado.getId()));
    }

    @Test
    void naoExcluiPagamentoComComprovante() {
        ordem.aceitar(comprador.getId());
        Pagamento p = pixSalvo("50.00");
        ordem.registrarPagamento(vendedor.getId(), p);
        ordem.anexarComprovante(comprador.getId(), p.getId(), "/arquivos/c.pdf");
        assertThrows(RegraDeNegocioException.class, () -> ordem.removerPagamento(vendedor.getId(), p.getId()));
    }

    @Test
    void pagamentoTemDataDeReferencia() {
        LocalDate dia = LocalDate.of(2026, 11, 20);
        Pagamento comData = Pagamento.criar(EnumTipoPagamento.BOLETO, BigDecimal.TEN, null, null, null, "Parcela 2", null, "34191.79001", dia);
        assertEquals(dia, comData.getDataReferencia());
        // sem informar: hoje
        assertEquals(LocalDate.now(), pix("10.00").getDataReferencia());
        // pagamento antigo (sem a coluna): usa o dia em que foi registrado
        LocalDateTime registrado = LocalDateTime.of(2026, 10, 1, 9, 30);
        Pagamento antigo = Pagamento.restaurar(UUID.randomUUID(), registrado, null, null, EnumTipoPagamento.PIX, BigDecimal.ONE,
                null, null, null, null, "chave", null, null);
        assertEquals(LocalDate.of(2026, 10, 1), antigo.getDataReferencia());
    }

    @Test
    void mensagensDeIncidenteNovasParaCadaLado() throws InterruptedException {
        assertEquals(0, ordem.mensagensIncidenteNaoLidas(vendedor.getId()));
        ordem.registrarMensagemIncidente(comprador.getId(), MensagemIncidente.criar("Carga avariada", null, comprador.getId()));
        ordem.registrarMensagemIncidente(comprador.getId(), MensagemIncidente.criar("Segue a foto", "/arquivos/f.png", comprador.getId()));
        assertEquals(2, ordem.mensagensIncidenteNaoLidas(vendedor.getId()));
        assertEquals(0, ordem.mensagensIncidenteNaoLidas(comprador.getId())); // as próprias não contam

        ordem.marcarIncidentesLidos(vendedor.getId());
        assertEquals(0, ordem.mensagensIncidenteNaoLidas(vendedor.getId()));

        Thread.sleep(5);
        ordem.registrarMensagemIncidente(vendedor.getId(), MensagemIncidente.criar("Vamos repor", null, vendedor.getId()));
        assertEquals(1, ordem.mensagensIncidenteNaoLidas(comprador.getId()));
        assertEquals(0, ordem.mensagensIncidenteNaoLidas(vendedor.getId()));
        assertEquals(0, ordem.mensagensIncidenteNaoLidas(UUID.randomUUID())); // quem não participa
        assertThrows(AcessoNegadoException.class, () -> ordem.marcarIncidentesLidos(UUID.randomUUID()));
    }

    @Test
    void entregaComDescricaoECodigoOpcionais() {
        Pagamento p = aceitarECadastrarTudo();
        // ao despachar, com só o código de envio
        ordem.enviar(vendedor.getId(), null, "  ", " BR123456789BR ");
        assertEquals("BR123456789BR", ordem.getEntrega().getCodigoRastreio());
        assertNull(ordem.getEntrega().getDescricao());

        // depois, o vendedor completa com a descrição
        ordem.registrarEntrega(vendedor.getId(), "Transportadora Rápida, entrega em horário comercial", "BR123456789BR");
        assertEquals("Transportadora Rápida, entrega em horário comercial", ordem.getEntrega().getDescricao());

        // só o vendedor informa; com os dois vazios a entrega sai
        assertThrows(AcessoNegadoException.class, () -> ordem.registrarEntrega(comprador.getId(), "x", null));
        ordem.registrarEntrega(vendedor.getId(), null, "");
        assertNull(ordem.getEntrega());
        assertThrows(IllegalArgumentException.class, () -> ordem.registrarEntrega(vendedor.getId(), null, "x".repeat(101)));
    }

    @Test
    void entregaSoDepoisDoAceite() {
        assertThrows(RegraDeNegocioException.class, () -> ordem.registrarEntrega(vendedor.getId(), "x", null));
        ordem.aceitar(comprador.getId());
        ordem.registrarEntrega(vendedor.getId(), "Retirada no CD", null); // pode adiantar antes do envio
        assertEquals("Retirada no CD", ordem.getEntrega().getDescricao());
    }

    @Test
    void atualizadaEmMudaQuandoAOrdemMudaMasNaoAoLerMensagens() throws InterruptedException {
        var inicio = ordem.getAtualizadaEm(); // sem mudanças: a data de criação
        Thread.sleep(5);
        ordem.aceitar(comprador.getId());
        var depoisDoAceite = ordem.getAtualizadaEm();
        assertTrue(depoisDoAceite.isAfter(inicio));

        Thread.sleep(5);
        ordem.marcarIncidentesLidos(vendedor.getId()); // só leitura: o outro lado não precisa recarregar
        assertEquals(depoisDoAceite, ordem.getAtualizadaEm());

        Thread.sleep(5);
        ordem.registrarMensagemIncidente(vendedor.getId(), MensagemIncidente.criar("Separando o pedido", null, vendedor.getId()));
        assertTrue(ordem.getAtualizadaEm().isAfter(depoisDoAceite));
    }

    // ------------------------------------------------------------ renegociação de valores

    private final UUID camiseta = UUID.randomUUID(), bone = UUID.randomUUID();

    private OrdemDePedido ordemComIds() {
        return OrdemDePedido.criar("PED-3", comprador, vendedor, List.of(
                ItemPedido.restaurar(camiseta, LocalDateTime.now(), null, null, UUID.randomUUID(), "Camiseta", "Azul", 2, new BigDecimal("30.00"), null),
                ItemPedido.restaurar(bone, LocalDateTime.now(), null, null, UUID.randomUUID(), "Boné", null, 1, new BigDecimal("40.00"), null)));
    }

    @Test
    void vendedorRenegociaOValorAntesDoAceiteEOCompradorEAvisado() {
        OrdemDePedido o = ordemComIds();
        var aviso = o.alterarValores(vendedor.getId(), java.util.Map.of(camiseta, new BigDecimal("25"), bone, new BigDecimal("40.00")));

        assertEquals(new BigDecimal("90.00"), o.getValorTotal()); // 2 x 25 + 40
        assertTrue(aviso.getMensagem().contains("Camiseta (Azul): R$\u00a030,00 → R$\u00a025,00 por unidade"), aviso.getMensagem());
        assertFalse(aviso.getMensagem().contains("Boné"), "item com o mesmo valor não entra no aviso");
        assertTrue(aviso.getMensagem().contains("Novo total: R$\u00a090,00 (antes R$\u00a0100,00)"), aviso.getMensagem());
        assertEquals(1, o.mensagensIncidenteNaoLidas(comprador.getId()));
        assertEquals(0, o.mensagensIncidenteNaoLidas(vendedor.getId()));

        o.aceitar(comprador.getId()); // o comprador aceita o valor novo
        assertEquals(EnumStatusOrdemDePedido.ACEITO, o.getStatus());
    }

    @Test
    void soOVendedorESoAntesDoAceite() {
        OrdemDePedido o = ordemComIds();
        var novo = java.util.Map.of(camiseta, new BigDecimal("20"));
        assertThrows(AcessoNegadoException.class, () -> o.alterarValores(comprador.getId(), novo));
        o.aceitar(comprador.getId());
        assertThrows(RegraDeNegocioException.class, () -> o.alterarValores(vendedor.getId(), novo));
    }

    @Test
    void recusaValorZeroItemDeOutraOrdemENadaAlterado() {
        OrdemDePedido o = ordemComIds();
        assertThrows(IllegalArgumentException.class, () -> o.alterarValores(vendedor.getId(), java.util.Map.of(camiseta, BigDecimal.ZERO)));
        assertThrows(RegraDeNegocioException.class, () -> o.alterarValores(vendedor.getId(), java.util.Map.of(UUID.randomUUID(), BigDecimal.TEN)));
        assertThrows(RegraDeNegocioException.class, () -> o.alterarValores(vendedor.getId(), java.util.Map.of(camiseta, new BigDecimal("30"))));
    }
}
