package com.psnf.api.psnf.domain.avaliacao;

import com.psnf.api.psnf.application.avaliacao.AvaliarAnuncioService;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.avaliacao.model.Avaliacao;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;
import com.psnf.api.psnf.domain.avaliacao.port.out.AvaliacaoRepositoryPort;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvaliacaoTest {

    final UUID vendedor = UUID.randomUUID();
    final UUID comprador = UUID.randomUUID();
    final UUID curioso = UUID.randomUUID();
    Anuncio anuncio;
    AvaliarAnuncioService service;
    final List<Avaliacao> banco = new ArrayList<>();

    @BeforeEach
    void setUp() {
        anuncio = Anuncio.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null, "Carrinho", "x", true, UUID.randomUUID(), vendedor,
                List.of(Valor.criar(BigDecimal.TEN, 1)), List.of(ImagemAnuncio.criar("a.png", true)), List.of(), null, List.of());
        AnuncioRepositoryPort anuncios = mock(AnuncioRepositoryPort.class);
        when(anuncios.buscarPorId(anuncio.getId())).thenReturn(anuncio);
        OrdemDePedidoRepositoryPort ordens = mock(OrdemDePedidoRepositoryPort.class);
        when(ordens.existeCompraEntregue(comprador, anuncio.getId())).thenReturn(true); // só o comprador recebeu
        UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
        when(usuarios.buscarPorId(any())).thenAnswer(i -> Usuario.restaurar(i.getArgument(0), LocalDateTime.now(), null, null,
                "Comprador", "c@x.com", "h", null, null, null, List.of()));
        AvaliacaoRepositoryPort avaliacoes = mock(AvaliacaoRepositoryPort.class);
        when(avaliacoes.save(any())).thenAnswer(i -> {
            Avaliacao a = i.getArgument(0);
            Avaliacao salva = a.getId() != null ? a : Avaliacao.restaurar(UUID.randomUUID(), a.getDataCadastro(), null, null,
                    a.getAnuncioId(), a.getAutorId(), a.getNota(), a.getTexto(), a.getImagens());
            banco.removeIf(x -> x.getId().equals(salva.getId()));
            banco.add(salva);
            return salva;
        });
        when(avaliacoes.listarPorAnuncio(any())).thenAnswer(i -> List.copyOf(banco));
        when(avaliacoes.buscarPorAnuncioEAutor(any(), any())).thenAnswer(i -> banco.stream()
                .filter(a -> a.getAutorId().equals(i.getArgument(1))).findFirst());
        doAnswer(i -> banco.removeIf(a -> a.getId().equals(i.getArgument(0)))).when(avaliacoes).remover(any());
        service = new AvaliarAnuncioService(avaliacoes, anuncios, ordens, usuarios);
    }

    @Test
    void validaNotaTextoEImagens() {
        UUID a = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> Avaliacao.criar(a, comprador, 0, null, null));
        assertThrows(IllegalArgumentException.class, () -> Avaliacao.criar(a, comprador, 6, null, null));
        assertThrows(IllegalArgumentException.class, () -> Avaliacao.criar(a, comprador, 5, "x".repeat(251), null));
        assertThrows(IllegalArgumentException.class, () -> Avaliacao.criar(a, comprador, 5, "ok", List.of("1", "2", "3", "4")));
        Avaliacao ok = Avaliacao.criar(a, comprador, 4, "  Ótimo  ", List.of("/arquivos/1.png", " ", "/arquivos/2.png"));
        assertEquals("Ótimo", ok.getTexto());
        assertEquals(List.of("/arquivos/1.png", "/arquivos/2.png"), ok.getImagens());
        assertNull(Avaliacao.criar(a, comprador, 3, "   ", null).getTexto()); // texto opcional
    }

    @Test
    void soQuemComprouERecebeuAvalia() {
        assertThrows(AcessoNegadoException.class, () -> service.avaliar(anuncio.getId(), vendedor, 5, "Meu produto é ótimo", null));
        assertThrows(AcessoNegadoException.class, () -> service.avaliar(anuncio.getId(), curioso, 1, "Nem comprei", null));
        var visao = service.listar(anuncio.getId(), curioso);
        assertFalse(visao.podeAvaliar());
        assertEquals("Só quem comprou e recebeu este produto pode avaliar", visao.motivoNaoPode());

        var r = service.avaliar(anuncio.getId(), comprador, 4, "Bom, chegou rápido", List.of("/arquivos/foto.png"));
        assertTrue(r.podeAvaliar());
        assertEquals(1, r.resumo().total());
        assertEquals(0, new BigDecimal("4.0").compareTo(r.resumo().media()));
        assertEquals(4, r.minha().getNota());
    }

    @Test
    void umaAvaliacaoPorCompradorQuePodeEditarEExcluir() {
        service.avaliar(anuncio.getId(), comprador, 2, "Veio riscado", null);
        var editada = service.avaliar(anuncio.getId(), comprador, 5, "Trocaram rápido, nota 5", null);
        assertEquals(1, editada.resumo().total());
        assertEquals(5, editada.minha().getNota());
        assertEquals(1, editada.resumo().porNota()[4]);
        var semAvaliacao = service.remover(anuncio.getId(), comprador);
        assertEquals(ResumoAvaliacoes.VAZIO, semAvaliacao.resumo());
        assertNull(semAvaliacao.minha());
    }

    @Test
    void mediaComUmaCasa() {
        UUID a = UUID.randomUUID();
        var r = ResumoAvaliacoes.de(List.of(Avaliacao.criar(a, comprador, 5, null, null), Avaliacao.criar(a, curioso, 4, null, null),
                Avaliacao.criar(a, vendedor, 4, null, null)));
        assertEquals(0, new BigDecimal("4.3").compareTo(r.media()));
        assertArrayEquals(new int[]{0, 0, 0, 2, 1}, r.porNota());
    }
}
