package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Caminho do comprovante já enviado para POST /arquivos. */
public record ComprovanteRequest(@NotBlank(message = "Envie o arquivo do comprovante") String caminho) {
}
