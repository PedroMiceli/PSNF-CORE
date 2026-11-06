package com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemNegociacaoRequest(
        @NotBlank(message = "Escreva uma mensagem")
        @Size(max = 2000, message = "A mensagem deve ter no máximo 2000 caracteres")
        String texto
) {
}
