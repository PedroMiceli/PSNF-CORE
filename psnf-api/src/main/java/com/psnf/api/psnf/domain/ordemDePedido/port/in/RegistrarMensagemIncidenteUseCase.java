package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarMensagemIncidenteCommand;

import java.util.UUID;

public interface RegistrarMensagemIncidenteUseCase {

    OrdemDePedido registrarMensagem(RegistrarMensagemIncidenteCommand command);

    /** O usuário viu as mensagens de incidente da ordem. */
    OrdemDePedido marcarLidas(UUID ordemDePedidoId, UUID usuarioId);
}
