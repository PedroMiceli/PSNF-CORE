package com.psnf.api.psnf.infrastructure.persistence.categoria.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Relações:
 *  - 1:N Anuncio (lado inverso, sem cascade)
 */
@Entity
@Table(name = "categoria")
@NoArgsConstructor
@Setter
@Getter
public class CategoriaJpa extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "categoria")
    private List<AnuncioJpa> anuncios = new ArrayList<>();
}
