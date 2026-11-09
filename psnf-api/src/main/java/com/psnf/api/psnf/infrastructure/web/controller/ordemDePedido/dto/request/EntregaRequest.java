package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.Size;

/** Os dois campos são opcionais; com os dois vazios a entrega é removida. */
public record EntregaRequest(
        @Size(max = 2000, message = "A descrição da entrega deve ter no máximo 2000 caracteres")
        String descricao,
        @Size(max = 100, message = "O código de envio deve ter no máximo 100 caracteres")
        String codigoRastreio
) {
}
