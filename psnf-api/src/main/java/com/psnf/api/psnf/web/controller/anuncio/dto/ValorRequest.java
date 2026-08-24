package com.psnf.api.psnf.web.controller.anuncio.dto;

import java.math.BigDecimal;

public record ValorRequest(
        BigDecimal valorUnidade,
        int quantidadeMinima
) {}
