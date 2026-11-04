package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

public record EnviarOrdemDePedidoRequest(
        @FutureOrPresent(message = "A previsão de entrega não pode estar no passado")
        LocalDate previsaoDeEntrega
) {
}
