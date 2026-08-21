package com.psnf.api.psnf.anuncio.adapters.outbound.entities;

import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import com.psnf.api.psnf.domain.models.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class JPAValor extends BaseEntity {

    BigDecimal valorUnidade;
    int quantidadeMinima;

    @ManyToOne
    @JoinColumn(name = "anuncio_id", nullable = false)
    private JPAAnuncio anuncio;

    public JPAValor(BigDecimal valorUnidade, int quantidadeMinima) {
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }
}
