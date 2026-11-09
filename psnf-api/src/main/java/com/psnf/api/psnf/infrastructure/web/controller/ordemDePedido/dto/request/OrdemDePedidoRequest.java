package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Enviado pelo VENDEDOR (usuário logado) após fechar o acordo com o comprador no chat.
 */
public record OrdemDePedidoRequest(
        @NotNull(message = "O comprador é obrigatório")
        UUID compradorId,
        @NotEmpty(message = "É necessário informar ao menos um item")
        List<@Valid ItemPedidoRequest> itens,
        /** opcional: negociação (chat) de onde a ordem saiu, para ficarem vinculadas */
        UUID negociacaoId
) {
}
