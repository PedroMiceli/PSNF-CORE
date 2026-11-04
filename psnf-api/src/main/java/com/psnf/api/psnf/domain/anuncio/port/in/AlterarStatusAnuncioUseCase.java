package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.UUID;

public interface AlterarStatusAnuncioUseCase {

    Anuncio ativar(UUID anuncioId, UUID usuarioId);

    Anuncio desativar(UUID anuncioId, UUID usuarioId);
}
