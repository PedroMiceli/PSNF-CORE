package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class MensagemIncidente extends BaseDomain {

    private final String mensagem;
    private final String imagem;
    private final UUID usuarioCriador;

    private MensagemIncidente(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String mensagem, String imagem, UUID usuarioCriador) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.mensagem = mensagem;
        this.imagem = imagem;
        this.usuarioCriador = usuarioCriador;
    }

    public static MensagemIncidente criar(String mensagem, String imagem, UUID usuarioCriador) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem é obrigatória");
        }
        return new MensagemIncidente(null, LocalDateTime.now(), null, null, mensagem, imagem, usuarioCriador);
    }

    public static MensagemIncidente restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String mensagem, String imagem, UUID usuarioCriador) {
        return new MensagemIncidente(id, dataCadastro, dataAlteracao, dataExcluido, mensagem, imagem, usuarioCriador);
    }

    public String getMensagem() { return mensagem; }
    public String getImagem() { return imagem; }
    public UUID getUsuarioCriador() { return usuarioCriador; }
}
