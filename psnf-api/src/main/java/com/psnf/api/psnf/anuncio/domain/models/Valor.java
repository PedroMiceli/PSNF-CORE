package com.psnf.api.psnf.anuncio.domain.models;

import com.psnf.api.psnf.domain.models.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
