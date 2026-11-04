package com.psnf.api.psnf.domain.ordemDePedido;

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
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_PAGAMENTO, ordem.getStatus());
    }

    @Test
    void naoPermiteComprarDeSiMesmo() {
        assertThrows(RegraDeNegocioException.class, () -> OrdemDePedido.criar("PED-2", comprador, comprador,
                List.of(ItemPedido.criar(UUID.randomUUID(), "X", null, 1, BigDecimal.ONE))));
    }

    @Test
    void fluxoCompletoAteEntrega() {
        ordem.registrarPagamento(comprador.getId(), pix("60.00"));
        assertEquals(EnumStatusOrdemDePedido.AGUARDANDO_PAGAMENTO, ordem.getStatus());

        ordem.registrarPagamento(comprador.getId(), pix("40.00"));
        assertEquals(EnumStatusOrdemDePedido.PAGO, ordem.getStatus());

        ordem.adicionarNotaFiscal(vendedor.getId(), NotaFiscal.criar("nf.pdf"));
        ordem.enviar(vendedor.getId(), LocalDate.now().plusDays(3));
        assertEquals(EnumStatusOrdemDePedido.ENVIADO, ordem.getStatus());

        ordem.confirmarEntrega(comprador.getId());
        assertEquals(EnumStatusOrdemDePedido.ENTREGUE, ordem.getStatus());
        assertEquals(1, ordem.getNotasFiscais().size());
    }

    @Test
    void naoAceitaPagamentoMaiorQueRestante() {
        assertThrows(RegraDeNegocioException.class, () -> ordem.registrarPagamento(comprador.getId(), pix("100.01")));
    }

    @Test
    void apenasCompradorPagaEApenasVendedorEnvia() {
        assertThrows(AcessoNegadoException.class, () -> ordem.registrarPagamento(vendedor.getId(), pix("10")));
        ordem.registrarPagamento(comprador.getId(), pix("100.00"));
        assertThrows(AcessoNegadoException.class, () -> ordem.enviar(comprador.getId(), null));
    }

    @Test
    void naoEnviaSemPagamento() {
        assertThrows(RegraDeNegocioException.class, () -> ordem.enviar(vendedor.getId(), null));
    }

    @Test
    void naoCancelaDepoisDeEnviada() {
        ordem.registrarPagamento(comprador.getId(), pix("100.00"));
        ordem.enviar(vendedor.getId(), null);
        assertThrows(RegraDeNegocioException.class, () -> ordem.cancelar(comprador.getId()));
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
}
