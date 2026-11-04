package com.psnf.api.psnf.domain.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnuncioTest {

    private Anuncio anuncio(List<Valor> valores, List<ImagemAnuncio> imagens) {
        return Anuncio.criar("Camiseta", "Algodão", UUID.randomUUID(), UUID.randomUUID(), valores, imagens, List.of());
    }

    @Test
    void calculaValorUnidadePelaFaixaDeQuantidade() {
        Anuncio a = anuncio(
                List.of(Valor.criar(new BigDecimal("30"), 1), Valor.criar(new BigDecimal("25"), 10), Valor.criar(new BigDecimal("20"), 100)),
                List.of(ImagemAnuncio.criar("a.png", false)));

        assertEquals(new BigDecimal("30"), a.calcularValorUnidade(1));
        assertEquals(new BigDecimal("30"), a.calcularValorUnidade(9));
        assertEquals(new BigDecimal("25"), a.calcularValorUnidade(10));
        assertEquals(new BigDecimal("20"), a.calcularValorUnidade(500));
        assertEquals(new BigDecimal("20"), a.getMenorValor());
    }

    @Test
    void rejeitaQuantidadeAbaixoDoMinimo() {
        Anuncio a = anuncio(List.of(Valor.criar(BigDecimal.TEN, 5)), List.of(ImagemAnuncio.criar("a.png", true)));
        assertThrows(RegraDeNegocioException.class, () -> a.calcularValorUnidade(4));
    }

    @Test
    void primeiraImagemViraPrincipalQuandoNenhumaForMarcada() {
        Anuncio a = anuncio(List.of(Valor.criar(BigDecimal.TEN, 1)),
                List.of(ImagemAnuncio.criar("a.png", false), ImagemAnuncio.criar("b.png", false)));
        assertEquals("a.png", a.getImagemPrincipal().orElseThrow().getCaminho());
    }

    @Test
    void rejeitaMaisDeUmaImagemPrincipal() {
        assertThrows(RegraDeNegocioException.class, () -> anuncio(List.of(Valor.criar(BigDecimal.TEN, 1)),
                List.of(ImagemAnuncio.criar("a.png", true), ImagemAnuncio.criar("b.png", true))));
    }

    @Test
    void rejeitaFaixasDePrecoDuplicadas() {
        assertThrows(RegraDeNegocioException.class, () -> anuncio(
                List.of(Valor.criar(BigDecimal.TEN, 1), Valor.criar(BigDecimal.ONE, 1)),
                List.of(ImagemAnuncio.criar("a.png", true))));
    }
}
