package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.UUID;

public record AnuncioResponse(
        UUID id,
        String titulo,
        String descricao,
        boolean ativo,
        UUID categoriaId
) {

    public static AnuncioResponse from(Anuncio anuncio) {
        return new AnuncioResponse(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getDescricao(),
                anuncio.isAtivo(),
                anuncio.getCategoria()
        );
    }
}
