package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AnuncioRequest(
        @NotBlank(message = "Título é obrigatório")
        String titulo,
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,
        @NotNull(message = "Categoria é obrigatória")
        UUID categoriaId,
        @NotEmpty(message = "É necessário informar ao menos um valor")
        @Valid
        List<ValorRequest> valores,
        @NotEmpty(message = "É necessário informar ao menos uma imagem")
        @Valid
        List<ImagemAnuncioRequest> imagens,
        List<VariacaoRequest> variacoes
) {}
