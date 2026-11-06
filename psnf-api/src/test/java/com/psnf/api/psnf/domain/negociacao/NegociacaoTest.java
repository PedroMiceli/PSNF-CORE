package com.psnf.api.psnf.domain.negociacao;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.model.enums.TipoMensagemNegociacao;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NegociacaoTest {

    final UUID vendedor = UUID.randomUUID();
    final UUID comprador = UUID.randomUUID();

    private Anuncio anuncio() {
        return Anuncio.criar("Carrinho de metal", "Para armazém", UUID.randomUUID(), vendedor,
                List.of(Valor.criar(BigDecimal.TEN, 1)), List.of(ImagemAnuncio.criar("a.png", true)), List.of());
    }

    @Test
    void comecaComMensagemDeAbordagemDoComprador() {
        Negociacao n = Negociacao.iniciar(anuncio(), comprador, "Vendedor Mock");
        assertEquals(vendedor, n.getVendedorId());
        assertEquals(1, n.getMensagens().size());
        var abordagem = n.getMensagens().getFirst();
        assertEquals(TipoMensagemNegociacao.ABORDAGEM, abordagem.getTipo());
        assertEquals(comprador, abordagem.getAutorId());
        assertTrue(abordagem.getTexto().startsWith("Olá, Vendedor Mock!"));
        assertTrue(abordagem.getTexto().contains("\"Carrinho de metal\""));
        assertEquals(abordagem.getDataCadastro(), n.getUltimaMensagemEm());
    }

    @Test
    void vendedorNaoNegociaOProprioAnuncioNemAnuncioInativo() {
        assertThrows(RegraDeNegocioException.class, () -> Negociacao.iniciar(anuncio(), vendedor, "x"));
        Anuncio inativo = anuncio();
        inativo.desativar();
        assertThrows(RegraDeNegocioException.class, () -> Negociacao.iniciar(inativo, comprador, "x"));
    }

    @Test
    void soParticipantesEnviamMensagens() {
        Negociacao n = Negociacao.iniciar(anuncio(), comprador, "Vendedor");
        n.enviarMensagem(vendedor, "  Olá! Temos pronta entrega.  ");
        assertEquals("Olá! Temos pronta entrega.", n.getUltimaMensagem().orElseThrow().getTexto());
        assertEquals(TipoMensagemNegociacao.TEXTO, n.getUltimaMensagem().orElseThrow().getTipo());
        assertThrows(AcessoNegadoException.class, () -> n.enviarMensagem(UUID.randomUUID(), "oi"));
        assertThrows(IllegalArgumentException.class, () -> n.enviarMensagem(comprador, "   "));
        assertThrows(IllegalArgumentException.class, () -> n.enviarMensagem(comprador, "a".repeat(2001)));
        assertEquals(2, n.getMensagens().size());
    }

    @Test
    void contaMensagensSemRespostaDeCadaParticipante() {
        Negociacao n = Negociacao.iniciar(anuncio(), comprador, "Vendedor");
        assertEquals(1, n.mensagensSemResposta(vendedor)); // a abordagem
        assertEquals(0, n.mensagensSemResposta(comprador));
        n.enviarMensagem(comprador, "Qual o prazo?");
        assertEquals(2, n.mensagensSemResposta(vendedor));
        n.enviarMensagem(vendedor, "5 dias");
        assertEquals(0, n.mensagensSemResposta(vendedor));
        assertEquals(1, n.mensagensSemResposta(comprador));
    }
}
