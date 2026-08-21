package com.psnf.api.psnf.anuncio.adapters.outbound.repositories;

import com.psnf.api.psnf.anuncio.domain.models.Anuncio;

public interface anuncioRepository {

    Anuncio save(Anuncio anuncio);
}
