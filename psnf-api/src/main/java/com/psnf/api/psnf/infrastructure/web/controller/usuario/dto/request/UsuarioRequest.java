package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UsuarioRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,
        @NotBlank(message = "Uma senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres")
        String senha,
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        @NotBlank(message = "Um telefone é obrigatório")
        String telefone,
        String imagem,
        List<@Valid EnderecoRequest> enderecos,
        /* opcional; com ou sem pontuação */
        String cnpj
) {
}
