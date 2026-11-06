package com.psnf.api.psnf.domain.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.Caracteristica;
import com.psnf.api.psnf.domain.anuncio.model.CodigosProduto;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.InformacoesProduto;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.domain.anuncio.model.enums.CondicaoProduto;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformacoesProdutoTest {

    private static InformacoesProduto info(String sku, String gtin, String ncm, String cest, Integer origem,
                                           BigDecimal liquido, BigDecimal bruto) {
        return new InformacoesProduto(sku, gtin, "Marca", "Modelo", CondicaoProduto.NOVO, ncm, cest, origem, "un", 1,
                liquido, bruto, null, null, null, 12, null, null);
    }

    @Test
    void gtinAceitaCodigosValidosERejeitaDigitoVerificadorErrado() {
        assertEquals("7891000315507", CodigosProduto.gtin("7891000315507"));
        assertEquals("12345670", CodigosProduto.gtin("1234 5670"));
        assertNull(CodigosProduto.gtin("SEM GTIN"));
        assertNull(CodigosProduto.gtin("  "));
        assertThrows(IllegalArgumentException.class, () -> CodigosProduto.gtin("7891000315508"));
        assertThrows(IllegalArgumentException.class, () -> CodigosProduto.gtin("123"));
    }

    @Test
    void ncmECestSaoFormatados() {
        assertEquals("7318.15.00", CodigosProduto.ncm("73181500"));
        assertEquals("7318.15.00", CodigosProduto.ncm("7318.15.00"));
        assertEquals("01.003.00", CodigosProduto.cest("0100300"));
        assertThrows(IllegalArgumentException.class, () -> CodigosProduto.ncm("7318"));
        assertThrows(IllegalArgumentException.class, () -> CodigosProduto.cest("123"));
    }

    @Test
    void normalizaEValidaOsCampos() {
        InformacoesProduto i = info(" ABC-1 ", null, "73181500", null, 0, new BigDecimal("1.2"), new BigDecimal("1.5"));
        assertEquals("ABC-1", i.sku());
        assertEquals("UN", i.unidadeComercial());
        assertEquals("7318.15.00", i.ncm());
        assertFalse(i.vazio());
        assertTrue(InformacoesProduto.VAZIO.vazio());

        assertThrows(IllegalArgumentException.class, () -> info(null, null, null, null, 9, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> info(null, null, null, null, null, new BigDecimal("2"), new BigDecimal("1")));
        assertThrows(IllegalArgumentException.class,
                () -> info(null, null, null, null, null, BigDecimal.ZERO, null));
    }

    @Test
    void variacaoSobrescreveSkuEGtinNaVenda() {
        Variacao azul = Variacao.criar("Azul", "CAM-AZ", "7891234567895");
        Variacao verde = Variacao.criar("Verde");
        Anuncio a = Anuncio.criar("Camiseta", "Algodão", UUID.randomUUID(), UUID.randomUUID(),
                List.of(Valor.criar(BigDecimal.TEN, 1)), List.of(ImagemAnuncio.criar("a.png", true)), List.of(azul, verde));
        a.definirDetalhes(info("CAM", "7891000315507", "61091000", null, 0, null, null), List.of(new Caracteristica("Tecido", "Algodão")));

        InformacoesProduto venda = a.informacoesParaVenda(azul);
        assertEquals("CAM-AZ", venda.sku());
        assertEquals("7891234567895", venda.gtin());
        assertEquals("6109.10.00", venda.ncm());

        assertEquals("CAM", a.informacoesParaVenda(verde).sku());
        assertEquals("7891000315507", a.informacoesParaVenda(null).gtin());
    }

    @Test
    void fichaTecnicaTemLimite() {
        Anuncio a = Anuncio.criar("Camiseta", "Algodão", UUID.randomUUID(), UUID.randomUUID(),
                List.of(Valor.criar(BigDecimal.TEN, 1)), List.of(ImagemAnuncio.criar("a.png", true)), List.of());
        List<Caracteristica> muitas = Collections.nCopies(Caracteristica.MAXIMO_POR_ANUNCIO + 1, new Caracteristica("a", "b"));
        assertThrows(RegraDeNegocioException.class, () -> a.definirDetalhes(null, muitas));
        a.definirDetalhes(null, null);
        assertTrue(a.getInformacoes().vazio());
        assertTrue(a.getCaracteristicas().isEmpty());
    }
}
