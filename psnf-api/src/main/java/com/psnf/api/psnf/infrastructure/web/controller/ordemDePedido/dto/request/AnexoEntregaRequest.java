package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.NotBlank;

/** caminho devolvido por POST /arquivos (foto ou PDF) */
public record AnexoEntregaRequest(@NotBlank(message = "Envie o arquivo e informe o caminho") String caminho) {
}
