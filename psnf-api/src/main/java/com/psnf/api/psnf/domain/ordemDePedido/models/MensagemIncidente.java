package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


public class MensagemIncidente extends BaseDomain {

    private String mensagem;

    private String imagem;

    private Usuario usuarioCriador;

    private OrdemDePedido ordemDePedido;

    public MensagemIncidente(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String mensagem, String imagem, Usuario usuarioCriador, OrdemDePedido ordemDePedido) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.mensagem = mensagem;
        this.imagem = imagem;
        this.usuarioCriador = usuarioCriador;
        this.ordemDePedido = ordemDePedido;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getImagem() {
        return imagem;
    }

    public Usuario getUsuarioCriador() {
        return usuarioCriador;
    }

    public OrdemDePedido getOrdemDePedido() {
        return ordemDePedido;
    }
}
