package com.psnf.api.psnf.infrastructure.web.controller.categoria.dto.response;

import com.psnf.api.psnf.domain.categoria.model.Categoria;

import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String nome
) {
    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
