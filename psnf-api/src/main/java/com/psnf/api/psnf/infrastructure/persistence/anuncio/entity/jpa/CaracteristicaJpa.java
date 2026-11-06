package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Item da ficha técnica do anúncio (nome = valor), na ordem em que foi informado. */
@Entity
@Table(name = "caracteristica_anuncio")
@NoArgsConstructor
@Getter
@Setter
public class CaracteristicaJpa extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 300)
    private String valor;

    private int posicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;
}
