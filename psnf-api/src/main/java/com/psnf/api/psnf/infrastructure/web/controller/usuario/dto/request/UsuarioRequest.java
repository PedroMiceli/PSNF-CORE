package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(
        @NotBlank(message = "Email é obrigatório")
        String email,
        @NotBlank(message = "Uma senha é obrigatória")
        String senha,
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        @NotBlank(message = "Um telefone é obrigatório")
        int telefone
) {

}
