package com.psnf.api.psnf.domain.anuncio.model;

import java.util.UUID;

/** Categoria em que um vendedor tem anúncios ativos, com a quantidade (filtro da página do vendedor). */
public record CategoriaComAnuncios(UUID categoriaId, String nome, long quantidade) {
}
