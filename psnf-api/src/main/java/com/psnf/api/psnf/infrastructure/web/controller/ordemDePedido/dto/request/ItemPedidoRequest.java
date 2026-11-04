package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemPedidoRequest(
        @NotNull(message = "Anúncio é obrigatório")
        UUID anuncioId,
        UUID variacaoId,
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantidade
) {
}
