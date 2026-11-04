package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "variacao_anuncio")
@NoArgsConstructor
@Setter
@Getter
public class VariacaoJpa extends BaseEntity {

    @Column(nullable = false)
    private String variacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;
}
