package com.psnf.api.psnf.anuncio.application.DTO.request;

public record ImagemAnuncioRequest(
        String caminho,
        boolean principal
) {}
