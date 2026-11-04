package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * O comprador é o usuário logado; o vendedor e os valores são obtidos dos anúncios.
 */
public record OrdemDePedidoRequest(
        @NotEmpty(message = "É necessário informar ao menos um item")
        List<@Valid ItemPedidoRequest> itens
) {
}
