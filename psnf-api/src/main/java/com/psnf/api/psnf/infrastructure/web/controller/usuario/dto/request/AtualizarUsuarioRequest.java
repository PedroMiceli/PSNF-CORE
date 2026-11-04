package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        String telefone,
        String imagem
) {
}
