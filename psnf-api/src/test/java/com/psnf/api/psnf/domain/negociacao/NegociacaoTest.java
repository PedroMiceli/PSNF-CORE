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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NegociacaoTest {

    final UUID vendedor = UUID.randomUUID();
    final UUID comprador = UUID.randomUUID();

    private Anuncio anuncio() {
        return anuncioDe(vendedor);
    }

    /** anúncio já salvo (com id), ativo */
    private static Anuncio anuncioDe(UUID dono) {
        return Anuncio.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null, "Carrinho de metal", "Para armazém", true,
                UUID.randomUUID(), dono, List.of(Valor.criar(BigDecimal.TEN, 1)), List.of(ImagemAnuncio.criar("a.png", true)),
                List.of(), null, List.of());
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

    @Test
    void incluiOutrosAnunciosDoMesmoVendedor() {
        Anuncio principal = anuncio();
        Negociacao n = Negociacao.iniciar(principal, comprador, "Vendedor");
        Anuncio outro = anuncio();
        n.adicionarAnuncio(comprador, outro);
        assertTrue(n.contemAnuncio(outro.getId()));
        assertEquals(2, n.getAnuncioIds().size());
        var aviso = n.getUltimaMensagem().orElseThrow();
        assertEquals(TipoMensagemNegociacao.ANUNCIO_ADICIONADO, aviso.getTipo());
        assertEquals(outro.getId(), aviso.getAnuncioId());
        assertEquals(2, n.mensagensSemResposta(vendedor)); // abordagem + aviso
    }

    @Test
    void regrasParaIncluirERetirarAnuncios() {
        Anuncio principal = anuncio();
        Negociacao n = Negociacao.iniciar(principal, comprador, "Vendedor");
        Anuncio deOutroVendedor = anuncioDe(UUID.randomUUID());
        assertThrows(RegraDeNegocioException.class, () -> n.adicionarAnuncio(comprador, deOutroVendedor));
        Anuncio inativo = anuncio();
        inativo.desativar();
        assertThrows(RegraDeNegocioException.class, () -> n.adicionarAnuncio(comprador, inativo));
        assertThrows(AcessoNegadoException.class, () -> n.adicionarAnuncio(UUID.randomUUID(), anuncio()));

        Anuncio extra = anuncio();
        n.adicionarAnuncio(vendedor, extra);
        assertThrows(RegraDeNegocioException.class, () -> n.adicionarAnuncio(comprador, extra)); // já está
        // o anúncio de abordagem nunca sai
        assertThrows(RegraDeNegocioException.class, () -> n.removerAnuncio(comprador, principal));
        n.removerAnuncio(comprador, extra);
        assertFalse(n.contemAnuncio(extra.getId()));
        assertEquals(TipoMensagemNegociacao.ANUNCIO_REMOVIDO, n.getUltimaMensagem().orElseThrow().getTipo());
        assertThrows(RegraDeNegocioException.class, () -> n.removerAnuncio(comprador, extra));
    }
}
