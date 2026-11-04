package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "imagem_anuncio")
@NoArgsConstructor
@Setter
@Getter
public class ImagemAnuncioJpa extends BaseEntity {

    @Column(nullable = false)
    private String caminho;

    private boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;
}
