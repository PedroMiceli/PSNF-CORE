package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response;

import com.psnf.api.psnf.domain.anuncio.model.AnuncioResumo;

import java.math.BigDecimal;
import java.util.UUID;

public record AnuncioResumoResponse(
        UUID id,
        String titulo,
        String descricao,
        String imagemPrincipal,
        BigDecimal precoMenor,
        UUID categoriaId,
        String categoriaNome,
        UUID vendedorId,
        String vendedorNome
) {
    public static AnuncioResumoResponse from(AnuncioResumo r) {
        return new AnuncioResumoResponse(r.id(), r.titulo(), r.descricao(), r.imagemPrincipal(), r.precoMenor(),
                r.categoriaId(), r.categoriaNome(), r.vendedorId(), r.vendedorNome());
    }
}
