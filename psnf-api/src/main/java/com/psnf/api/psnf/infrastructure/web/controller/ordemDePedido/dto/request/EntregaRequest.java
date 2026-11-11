package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.Size;

/** Todos opcionais; sem descrição, sem código e sem retirada no local, a entrega é removida. */
public record EntregaRequest(
        @Size(max = 2000, message = "A descrição da entrega deve ter no máximo 2000 caracteres")
        String descricao,
        @Size(max = 100, message = "O código de envio deve ter no máximo 100 caracteres")
        String codigoRastreio,
        /** opcional: o comprador retira no fornecedor (sem código de rastreio); ausente = mantém */
        Boolean retiradaNoLocal
) {
}
