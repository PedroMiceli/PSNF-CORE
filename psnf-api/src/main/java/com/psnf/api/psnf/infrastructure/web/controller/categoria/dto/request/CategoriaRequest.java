package com.psnf.api.psnf.infrastructure.web.controller.categoria.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "O nome da categoria é obrigatório")
        String nome
) {
}
