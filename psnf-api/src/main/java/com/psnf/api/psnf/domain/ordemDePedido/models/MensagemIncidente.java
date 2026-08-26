package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class MensagemIncidente extends BaseEntity {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String mensagem;

    private String imagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criador_id", nullable = false)
    private Usuario usuarioCriador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedido ordemDePedido;

//    // com UUID solto (como você tinha)
//    boolean minhaMensagem = mensagem.getUsuarioCriador().equals(usuarioLogado.getId());
//
//    // com @ManyToOne (como sugeri)
//    boolean minhaMensagem = mensagem.getUsuarioCriador().getId().equals(usuarioLogado.getId());
}
