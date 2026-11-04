package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.util.UUID;

public record ItemPedidoCommand(
        UUID anuncioId,
        UUID variacaoId,
        int quantidade
) {
    public ItemPedidoCommand {
        if (anuncioId == null) {
            throw new IllegalArgumentException("Anúncio do item é obrigatório");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}
