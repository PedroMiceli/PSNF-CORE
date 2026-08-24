package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.models.BaseEntity;

import java.math.BigDecimal;


public class Valor extends BaseEntity {

    BigDecimal valorUnidade;
    int quantidadeMinima;

    private Anuncio anuncio;

    public Valor() {
    }

    public Valor(BigDecimal valorUnidade, int quantidadeMinima) {
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    public BigDecimal getValorUnidade() {
        return valorUnidade;
    }

    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
}
