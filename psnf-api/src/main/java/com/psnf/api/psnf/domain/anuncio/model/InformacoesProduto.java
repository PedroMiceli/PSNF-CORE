package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.anuncio.model.enums.CondicaoProduto;
import com.psnf.api.psnf.domain.anuncio.model.enums.ResponsavelGarantia;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Informações complementares do produto, TODAS opcionais. Servem para facilitar a vida dos usuários:
 * dados que a NF-e pede (SKU, GTIN, NCM, CEST, origem, unidade), logística (pesos e medidas) e
 * informações para o comprador (condição, garantia, certificações).
 * São copiadas para o item da ordem de pedido no momento da compra.
 *
 * @param origem origem da mercadoria (tabela da NF-e, 0 a 8; 0 = nacional)
 * @param unidadeComercial ex.: UN, CX, PCT, KG (até 6 caracteres, como na NF-e)
 * @param certificacoes registros obrigatórios (INMETRO, ANVISA, CA de EPI...), texto livre
 */
public record InformacoesProduto(
        String sku,
        String gtin,
        String marca,
        String modelo,
        CondicaoProduto condicao,
        String ncm,
        String cest,
        Integer origem,
        String unidadeComercial,
        Integer quantidadePorEmbalagem,
        BigDecimal pesoLiquidoKg,
        BigDecimal pesoBrutoKg,
        BigDecimal comprimentoCm,
        BigDecimal larguraCm,
        BigDecimal alturaCm,
        Integer garantiaMeses,
        ResponsavelGarantia garantiaResponsavel,
        String certificacoes
) {
    public static final InformacoesProduto VAZIO = new InformacoesProduto(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    public InformacoesProduto {
        sku = CodigosProduto.sku(sku);
        gtin = CodigosProduto.gtin(gtin);
        marca = CodigosProduto.texto(marca, 120, "Marca");
        modelo = CodigosProduto.texto(modelo, 120, "Modelo");
        ncm = CodigosProduto.ncm(ncm);
        cest = CodigosProduto.cest(cest);
        if (origem != null && (origem < 0 || origem > 8)) {
            throw new IllegalArgumentException("Origem da mercadoria inválida: use um código de 0 a 8");
        }
        unidadeComercial = CodigosProduto.texto(unidadeComercial, 6, "Unidade comercial");
        if (unidadeComercial != null) unidadeComercial = unidadeComercial.toUpperCase();
        if (quantidadePorEmbalagem != null && quantidadePorEmbalagem < 1) {
            throw new IllegalArgumentException("Quantidade por embalagem deve ser maior que zero");
        }
        positivo(pesoLiquidoKg, "Peso líquido");
        positivo(pesoBrutoKg, "Peso bruto");
        positivo(comprimentoCm, "Comprimento");
        positivo(larguraCm, "Largura");
        positivo(alturaCm, "Altura");
        if (pesoLiquidoKg != null && pesoBrutoKg != null && pesoLiquidoKg.compareTo(pesoBrutoKg) > 0) {
            throw new IllegalArgumentException("O peso líquido não pode ser maior que o peso bruto");
        }
        if (garantiaMeses != null && (garantiaMeses < 0 || garantiaMeses > 600)) {
            throw new IllegalArgumentException("Garantia deve ser entre 0 e 600 meses");
        }
        certificacoes = CodigosProduto.texto(certificacoes, 1000, "Certificações");
    }

    private static void positivo(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero");
        }
    }

    public static InformacoesProduto ouVazio(InformacoesProduto informacoes) {
        return informacoes == null ? VAZIO : informacoes;
    }

    /** Cópia com o SKU/GTIN da variação escolhida (quando a variação tiver os seus). */
    public InformacoesProduto comCodigosDaVariacao(String skuVariacao, String gtinVariacao) {
        return new InformacoesProduto(
                skuVariacao != null ? skuVariacao : sku, gtinVariacao != null ? gtinVariacao : gtin,
                marca, modelo, condicao, ncm, cest, origem, unidadeComercial, quantidadePorEmbalagem,
                pesoLiquidoKg, pesoBrutoKg, comprimentoCm, larguraCm, alturaCm, garantiaMeses, garantiaResponsavel, certificacoes);
    }

    public boolean vazio() {
        return Stream.of(sku, gtin, marca, modelo, condicao, ncm, cest, origem, unidadeComercial, quantidadePorEmbalagem,
                        pesoLiquidoKg, pesoBrutoKg, comprimentoCm, larguraCm, alturaCm, garantiaMeses, garantiaResponsavel, certificacoes)
                .allMatch(Objects::isNull);
    }
}
