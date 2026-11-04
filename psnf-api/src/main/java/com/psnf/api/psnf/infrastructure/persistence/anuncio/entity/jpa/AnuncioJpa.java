package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.categoria.entity.CategoriaJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Relações:
 *  - N:1 Categoria
 *  - N:1 Usuario (vendedor)
 *  - 1:N Valor / ImagemAnuncio / Variacao (dono do ciclo de vida: cascade + orphanRemoval)
 */
@Entity
@Table(name = "anuncio")
@NoArgsConstructor
@Getter
@Setter
public class AnuncioJpa extends BaseEntity {

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaJpa categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private UsuarioJpa vendedor;

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("quantidadeMinima ASC")
    private List<ValorJpa> valores = new ArrayList<>();

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagemAnuncioJpa> imagens = new ArrayList<>();

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariacaoJpa> variacoes = new ArrayList<>();
}
