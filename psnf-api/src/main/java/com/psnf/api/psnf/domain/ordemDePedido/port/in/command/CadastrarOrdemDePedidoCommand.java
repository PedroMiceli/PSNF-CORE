package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.util.List;
import java.util.UUID;

/**
 * O vendedor não é informado: ele é derivado dos anúncios dos itens.
 * O valor unitário também não: ele é calculado a partir das faixas de preço do anúncio.
 */
public record CadastrarOrdemDePedidoCommand(
        UUID compradorId,
        List<ItemPedidoCommand> itens
) {
    public CadastrarOrdemDePedidoCommand {
        if (compradorId == null) {
            throw new IllegalArgumentException("Comprador é obrigatório");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar ao menos um item");
        }
    }
}
