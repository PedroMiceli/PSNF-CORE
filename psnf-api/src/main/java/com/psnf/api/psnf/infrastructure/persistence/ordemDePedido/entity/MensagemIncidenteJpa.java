package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relações:
 *  - N:1 OrdemDePedido
 *  - N:1 Usuario (quem escreveu a mensagem)
 */
@Entity
@Table(name = "mensagem_incidente")
@NoArgsConstructor
@Setter
@Getter
public class MensagemIncidenteJpa extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    private String imagem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_criador_id", nullable = false)
    private UsuarioJpa usuarioCriador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedidoJpa ordemDePedido;
}
