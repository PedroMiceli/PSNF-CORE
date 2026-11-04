package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotBlank(message = "Logradouro é obrigatório")
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        @NotBlank(message = "Cidade é obrigatória")
        String cidade,
        String estado,
        @NotBlank(message = "CEP é obrigatório")
        String cep,
        @Size(min = 2, max = 2, message = "UF deve ter 2 letras")
        String uf
) {
}
