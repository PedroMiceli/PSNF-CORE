package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ImagemAnuncioRequest(
        @NotBlank(message = "Caminho da imagem é obrigatório")
        String caminho,
        boolean principal
) {}
