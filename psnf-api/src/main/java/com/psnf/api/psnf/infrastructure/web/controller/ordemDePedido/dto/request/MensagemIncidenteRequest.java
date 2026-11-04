package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MensagemIncidenteRequest(
        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem,
        String imagem
) {
}
