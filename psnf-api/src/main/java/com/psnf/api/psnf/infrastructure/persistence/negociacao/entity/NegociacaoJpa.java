package com.psnf.api.psnf.infrastructure.persistence.negociacao.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Relações:
 *  - N:1 Anuncio (anúncio de abordagem)
 *  - N:1 Usuario (comprador)
 *  - N:1 Usuario (vendedor)
 *  - 1:N MensagemNegociacao (cascade + orphanRemoval)
 * Uma negociação por comprador e anúncio.
 */
@Entity
@Table(name = "negociacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_negociacao_anuncio_comprador", columnNames = {"anuncio_id", "comprador_id"}),
        indexes = {
                @Index(name = "idx_negociacao_comprador", columnList = "comprador_id"),
                @Index(name = "idx_negociacao_vendedor", columnList = "vendedor_id")
        })
@NoArgsConstructor
@Setter
@Getter
public class NegociacaoJpa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private UsuarioJpa comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private UsuarioJpa vendedor;

    /** usado para ordenar a lista de conversas pela mais recente */
    private LocalDateTime ultimaMensagemEm;

    @OneToMany(mappedBy = "negociacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCadastro ASC")
    private List<MensagemNegociacaoJpa> mensagens = new ArrayList<>();
}
