package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.util.UUID;

public record AdicionarNotaFiscalCommand(
        UUID ordemDePedidoId,
        UUID usuarioId,
        String caminho
) {
    public AdicionarNotaFiscalCommand {
        if (ordemDePedidoId == null || usuarioId == null) {
            throw new IllegalArgumentException("Ordem de pedido e usuário são obrigatórios");
        }
    }
}
