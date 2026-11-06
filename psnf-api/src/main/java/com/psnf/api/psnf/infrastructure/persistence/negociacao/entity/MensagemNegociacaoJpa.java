package com.psnf.api.psnf.infrastructure.persistence.negociacao.entity;

import com.psnf.api.psnf.domain.negociacao.model.enums.TipoMensagemNegociacao;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relações:
 *  - N:1 Negociacao
 *  - N:1 Usuario (autor)
 *  - N:1 Anuncio (opcional: produto citado na abordagem ou ao incluir/retirar um anúncio)
 */
@Entity
@Table(name = "mensagem_negociacao")
@NoArgsConstructor
@Setter
@Getter
public class MensagemNegociacaoJpa extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMensagemNegociacao tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private UsuarioJpa autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anuncio_id")
    private AnuncioJpa anuncio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negociacao_id", nullable = false)
    private NegociacaoJpa negociacao;
}
