package com.psnf.api.psnf.infrastructure.persistence.avaliacao.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Avaliação de um produto por um comprador.
 * Relações:
 *  - N:1 Anuncio
 *  - N:1 Usuario (autor, o comprador)
 *  - imagens: até 3 caminhos (tabela avaliacao_imagem, na ordem enviada)
 * Uma avaliação por comprador e anúncio.
 */
@Entity
@Table(name = "avaliacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_avaliacao_anuncio_autor", columnNames = {"anuncio_id", "autor_id"}),
        indexes = @Index(name = "idx_avaliacao_anuncio", columnList = "anuncio_id"))
@NoArgsConstructor
@Setter
@Getter
public class AvaliacaoJpa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private UsuarioJpa autor;

    /** 1 a 5 estrelas */
    @Column(nullable = false)
    private int nota;

    @Column(length = 250)
    private String texto;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "avaliacao_imagem", joinColumns = @JoinColumn(name = "avaliacao_id"))
    @OrderColumn(name = "posicao")
    @Column(name = "caminho", nullable = false)
    private List<String> imagens = new ArrayList<>();
}
