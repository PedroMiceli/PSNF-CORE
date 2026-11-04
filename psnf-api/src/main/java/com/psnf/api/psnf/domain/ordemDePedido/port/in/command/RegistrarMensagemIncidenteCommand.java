package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.util.UUID;

public record RegistrarMensagemIncidenteCommand(
        UUID ordemDePedidoId,
        UUID usuarioId,
        String mensagem,
        String imagem
) {
    public RegistrarMensagemIncidenteCommand {
        if (ordemDePedidoId == null || usuarioId == null) {
            throw new IllegalArgumentException("Ordem de pedido e usuário são obrigatórios");
        }
    }
}
