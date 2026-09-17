package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class MensagemIncidenteJpa extends BaseEntity {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String mensagem;

    private String imagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criador_id", nullable = false)
    private UsuarioJpa usuarioCriador;


    public MensagemIncidenteJpa(UUID id, String mensagem, String imagem, UsuarioJpa usuarioCriador) {
        this.setIdAndDate(id);
        this.mensagem = mensagem;
        this.imagem = imagem;
        this.usuarioCriador = usuarioCriador;
    }
}
