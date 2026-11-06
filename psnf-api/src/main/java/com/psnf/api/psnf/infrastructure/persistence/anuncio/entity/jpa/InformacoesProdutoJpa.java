package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.domain.anuncio.model.enums.CondicaoProduto;
import com.psnf.api.psnf.domain.anuncio.model.enums.ResponsavelGarantia;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Informações complementares do produto (todas opcionais). Usado em "anuncio" e copiado em "item_pedido".
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InformacoesProdutoJpa {

    @Column(length = 60)
    private String sku;
    @Column(length = 14)
    private String gtin;
    @Column(length = 120)
    private String marca;
    @Column(length = 120)
    private String modelo;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CondicaoProduto condicao;
    @Column(length = 10)
    private String ncm;
    @Column(length = 9)
    private String cest;
    private Integer origem;
    @Column(length = 6)
    private String unidadeComercial;
    private Integer quantidadePorEmbalagem;
    @Column(precision = 12, scale = 3)
    private BigDecimal pesoLiquidoKg;
    @Column(precision = 12, scale = 3)
    private BigDecimal pesoBrutoKg;
    @Column(precision = 10, scale = 2)
    private BigDecimal comprimentoCm;
    @Column(precision = 10, scale = 2)
    private BigDecimal larguraCm;
    @Column(precision = 10, scale = 2)
    private BigDecimal alturaCm;
    private Integer garantiaMeses;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ResponsavelGarantia garantiaResponsavel;
    @Column(columnDefinition = "TEXT")
    private String certificacoes;
}
