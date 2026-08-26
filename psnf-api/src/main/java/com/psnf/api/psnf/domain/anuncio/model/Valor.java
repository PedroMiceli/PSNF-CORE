package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public class Valor extends BaseDomain {

    BigDecimal valorUnidade;
    int quantidadeMinima;

    private Anuncio anuncio;

    public Valor() {
    }

    public Valor(UUID id, BigDecimal valorUnidade, int quantidadeMinima) {
        super(id, LocalDateTime.now(),null,null);
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    public Valor(BigDecimal valorUnidade, int quantidadeMinima) {
        super(null, LocalDateTime.now(),null,null);
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    public static Valor criar(BigDecimal valorUnidade, int quantidadeMinima) {
        return new Valor(valorUnidade, quantidadeMinima);
    }

    public BigDecimal getValorUnidade() {
        return valorUnidade;
    }

    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
}
