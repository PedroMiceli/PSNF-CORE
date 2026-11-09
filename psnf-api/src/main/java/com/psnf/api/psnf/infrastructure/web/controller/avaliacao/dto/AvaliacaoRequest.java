package com.psnf.api.psnf.infrastructure.web.controller.avaliacao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** @param imagens caminhos enviados antes para POST /arquivos (até 3) */
public record AvaliacaoRequest(
        @NotNull(message = "Dê uma nota de 1 a 5 estrelas")
        @Min(value = 1, message = "A nota deve ser de 1 a 5 estrelas")
        @Max(value = 5, message = "A nota deve ser de 1 a 5 estrelas")
        Integer nota,
        @Size(max = 250, message = "O texto deve ter no máximo 250 caracteres")
        String texto,
        @Size(max = 3, message = "Envie no máximo 3 imagens")
        List<String> imagens
) {
}
