package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.UUID;

public interface BuscarAnuncioPorIdUseCase {

    /** {@code visitanteId}: sem CNPJ, anúncio exclusivo para empresas responde 404 */
    Anuncio buscarPorId(UUID anuncioId, UUID visitanteId);

}
