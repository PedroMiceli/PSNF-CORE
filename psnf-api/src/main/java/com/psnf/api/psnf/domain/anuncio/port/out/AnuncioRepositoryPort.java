package com.psnf.api.psnf.domain.anuncio.port.out;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.List;
import java.util.UUID;

public interface AnuncioRepositoryPort {

    Anuncio save(Anuncio anuncio);

    Anuncio buscarPorId(UUID anuncioId);

    List<Anuncio> buscarPorVendedor(UUID vendedorId);
}
