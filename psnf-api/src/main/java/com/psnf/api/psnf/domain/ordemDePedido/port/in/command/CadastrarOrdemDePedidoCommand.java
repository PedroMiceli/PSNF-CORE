package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.util.List;
import java.util.UUID;

/**
 * A ordem é criada pelo VENDEDOR para um comprador, após os dois chegarem a um acordo no chat.
 * Por isso o valor unitário de cada item é o valor negociado, informado pelo vendedor.
 */
public record CadastrarOrdemDePedidoCommand(
        UUID vendedorId,
        UUID compradorId,
        List<ItemPedidoCommand> itens,
        /** opcional: negociação (chat) de onde a ordem saiu */
        UUID negociacaoId
) {
    public CadastrarOrdemDePedidoCommand {
        if (vendedorId == null) {
            throw new IllegalArgumentException("Vendedor é obrigatório");
        }
        if (compradorId == null) {
            throw new IllegalArgumentException("Comprador é obrigatório");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar ao menos um item");
        }
    }
}
