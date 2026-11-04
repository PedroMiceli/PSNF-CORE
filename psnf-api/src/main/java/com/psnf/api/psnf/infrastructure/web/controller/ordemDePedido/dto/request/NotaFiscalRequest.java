package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NotaFiscalRequest(
        @NotBlank(message = "Caminho da nota fiscal é obrigatório")
        String caminho
) {
}
