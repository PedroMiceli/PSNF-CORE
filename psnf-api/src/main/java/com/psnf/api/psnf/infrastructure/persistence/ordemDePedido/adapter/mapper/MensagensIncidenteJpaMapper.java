package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.MensagemIncidente;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.MensagemIncidenteJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

public class MensagensIncidenteJpaMapper {

    public static MensagemIncidenteJpa toEntity(MensagemIncidente mensagemIncidente) {
        return new MensagemIncidenteJpa(
                mensagemIncidente.getId(),
                mensagemIncidente.getMensagem(),
                mensagemIncidente.getImagem(),
                new UsuarioJpa(mensagemIncidente.getUsuarioCriador())
        );
    }

    //transforma o jpa para domain
    public static MensagemIncidente toDomain(MensagemIncidenteJpa mensagemIncidenteJpa) {
        return MensagemIncidente.restaurar(
                mensagemIncidenteJpa.getId(),
                mensagemIncidenteJpa.getDataCadastro(),
                mensagemIncidenteJpa.getDataAlteracao(),
                mensagemIncidenteJpa.getDataExcluido(),
                mensagemIncidenteJpa.getMensagem(),
                mensagemIncidenteJpa.getImagem(),
                mensagemIncidenteJpa.getUsuarioCriador().getId()
        );
    }
}
