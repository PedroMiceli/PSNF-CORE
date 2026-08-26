package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class ValorJpa extends BaseEntity {

    BigDecimal valorUnidade;
    int quantidadeMinima;

    @ManyToOne
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;

    public ValorJpa(UUID id, BigDecimal valorUnidade, int quantidadeMinima) {
        this.setId(id);
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }
}
