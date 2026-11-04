package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.MensagemIncidente;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.MensagemIncidenteJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

public class MensagensIncidenteJpaMapper {

    public static MensagemIncidenteJpa toEntity(MensagemIncidente mensagem, UsuarioJpa usuarioCriador, OrdemDePedidoJpa ordemDePedido) {
        MensagemIncidenteJpa jpa = new MensagemIncidenteJpa();
        jpa.restaurarBase(mensagem.getId(), mensagem.getDataCadastro(), mensagem.getDataAlteracao(), mensagem.getDataExcluido());
        jpa.setMensagem(mensagem.getMensagem());
        jpa.setImagem(mensagem.getImagem());
        jpa.setUsuarioCriador(usuarioCriador);
        jpa.setOrdemDePedido(ordemDePedido);
        return jpa;
    }

    //transforma o jpa para domain
    public static MensagemIncidente toDomain(MensagemIncidenteJpa jpa) {
        return MensagemIncidente.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getMensagem(),
                jpa.getImagem(),
                jpa.getUsuarioCriador().getId()
        );
    }
}
