package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ValorRequest(
        @NotNull(message = "Valor unitário é obrigatório")
        @Positive(message = "Valor unitário deve ser maior que zero")
        BigDecimal valorUnidade,

        @Positive(message = "Quantidade mínima deve ser maior que zero")
        int quantidadeMinima
) {}
