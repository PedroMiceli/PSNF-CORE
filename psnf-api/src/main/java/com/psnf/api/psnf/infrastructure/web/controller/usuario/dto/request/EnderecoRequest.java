package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(
        @NotBlank(message = "Logradouro é obrigatório")
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        @NotBlank(message = "Cidade é obrigatória")
        String cidade,
        @NotBlank(message = "CEP é obrigatório")
        String cep,
        @NotBlank(message = "UF é obrigatória")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve ser a sigla do estado com 2 letras, ex.: SP")
        String uf
) {
}
