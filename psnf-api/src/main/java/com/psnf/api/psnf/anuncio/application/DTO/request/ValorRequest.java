package com.psnf.api.psnf.anuncio.application.DTO.request;

import java.math.BigDecimal;

public record ValorRequest(
        BigDecimal valorUnidade,
        int quantidadeMinima
) {}
