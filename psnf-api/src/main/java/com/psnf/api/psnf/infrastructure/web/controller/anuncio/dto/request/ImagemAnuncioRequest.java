package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ImagemAnuncioRequest(
        @NotBlank(message = "Caminho da imagem é obrigatório")
        String caminho,
        // wrapper: campo opcional no JSON (o Jackson 3 rejeita primitivos ausentes)
        Boolean principal
) {
    public boolean isPrincipal() {
        return Boolean.TRUE.equals(principal);
    }
}
