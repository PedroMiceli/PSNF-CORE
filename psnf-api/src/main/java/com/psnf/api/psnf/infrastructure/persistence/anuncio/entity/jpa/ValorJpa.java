package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "valor_anuncio")
@NoArgsConstructor
@Setter
@Getter
public class ValorJpa extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorUnidade;

    private int quantidadeMinima;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;
}
