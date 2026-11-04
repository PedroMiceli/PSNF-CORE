package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VariacaoRequest(
        @NotBlank(message = "Variação é obrigatória")
        String variacao
) {}
