package com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IniciarNegociacaoRequest(@NotNull(message = "Informe o anúncio") UUID anuncioId) {
}
