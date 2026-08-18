package com.psnf.api.psnf.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Valor extends BaseEntity{

    float valorUnidade;
    int quantidadeMinima;

    @ManyToOne
    @JoinColumn(name = "anuncio_id", nullable = false)
    private Anuncio anuncio;

    public Valor(float valorUnidade, int quantidadeMinima) {
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }
}
