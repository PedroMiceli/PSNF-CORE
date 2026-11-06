package com.psnf.api.psnf.infrastructure.persistence.negociacao.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Anúncio incluído numa negociação além do anúncio de abordagem.
 * Relações:
 *  - N:1 Negociacao
 *  - N:1 Anuncio
 *  - N:1 Usuario (quem incluiu)
 */
@Entity
@Table(name = "negociacao_anuncio",
        uniqueConstraints = @UniqueConstraint(name = "uk_negociacao_anuncio", columnNames = {"negociacao_id", "anuncio_id"}))
@NoArgsConstructor
@Setter
@Getter
public class NegociacaoAnuncioJpa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negociacao_id", nullable = false)
    private NegociacaoJpa negociacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adicionado_por_id", nullable = false)
    private UsuarioJpa adicionadoPor;
}
