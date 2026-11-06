package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto;

import com.psnf.api.psnf.domain.anuncio.model.InformacoesProduto;
import com.psnf.api.psnf.domain.anuncio.model.enums.CondicaoProduto;
import com.psnf.api.psnf.domain.anuncio.model.enums.ResponsavelGarantia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Informações complementares do produto (todas opcionais), na entrada e na saída da API.
 * O formato é validado aqui (erros por campo); as regras completas (dígito do GTIN etc.) no domínio.
 */
public record InformacoesProdutoDto(
        @Size(max = 60, message = "SKU deve ter no máximo 60 caracteres") String sku,
        @Pattern(regexp = "^\\s*(\\d{8}|\\d{12}|\\d{13}|\\d{14}|(?i:sem gtin))?\\s*$", message = "GTIN/EAN deve ter 8, 12, 13 ou 14 dígitos")
        String gtin,
        @Size(max = 120, message = "Marca deve ter no máximo 120 caracteres") String marca,
        @Size(max = 120, message = "Modelo deve ter no máximo 120 caracteres") String modelo,
        CondicaoProduto condicao,
        @Pattern(regexp = "^\\s*(\\d{4}\\.?\\d{2}\\.?\\d{2})?\\s*$", message = "NCM deve ter 8 dígitos (ex.: 8716.80.00)")
        String ncm,
        @Pattern(regexp = "^\\s*(\\d{2}\\.?\\d{3}\\.?\\d{2})?\\s*$", message = "CEST deve ter 7 dígitos (ex.: 01.003.00)")
        String cest,
        @Min(value = 0, message = "Origem deve ser de 0 a 8") @Max(value = 8, message = "Origem deve ser de 0 a 8") Integer origem,
        @Size(max = 6, message = "Unidade deve ter no máximo 6 caracteres") String unidadeComercial,
        @Positive(message = "Deve ser maior que zero") Integer quantidadePorEmbalagem,
        @Positive(message = "Deve ser maior que zero") BigDecimal pesoLiquidoKg,
        @Positive(message = "Deve ser maior que zero") BigDecimal pesoBrutoKg,
        @Positive(message = "Deve ser maior que zero") BigDecimal comprimentoCm,
        @Positive(message = "Deve ser maior que zero") BigDecimal larguraCm,
        @Positive(message = "Deve ser maior que zero") BigDecimal alturaCm,
        @Min(value = 0, message = "Garantia não pode ser negativa") @Max(value = 600, message = "Garantia de no máximo 600 meses") Integer garantiaMeses,
        ResponsavelGarantia garantiaResponsavel,
        @Size(max = 1000, message = "Certificações devem ter no máximo 1000 caracteres") String certificacoes
) {
    public InformacoesProduto toDomain() {
        return new InformacoesProduto(sku, gtin, marca, modelo, condicao, ncm, cest, origem, unidadeComercial, quantidadePorEmbalagem,
                pesoLiquidoKg, pesoBrutoKg, comprimentoCm, larguraCm, alturaCm, garantiaMeses, garantiaResponsavel, certificacoes);
    }

    public static InformacoesProdutoDto from(InformacoesProduto i) {
        return new InformacoesProdutoDto(i.sku(), i.gtin(), i.marca(), i.modelo(), i.condicao(), i.ncm(), i.cest(), i.origem(),
                i.unidadeComercial(), i.quantidadePorEmbalagem(), i.pesoLiquidoKg(), i.pesoBrutoKg(), i.comprimentoCm(),
                i.larguraCm(), i.alturaCm(), i.garantiaMeses(), i.garantiaResponsavel(), i.certificacoes());
    }
}
