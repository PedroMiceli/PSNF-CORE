package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.UUID;

public interface BuscarAnuncioPorIdUseCase {

    Anuncio buscarPorId(UUID anuncio);

}
