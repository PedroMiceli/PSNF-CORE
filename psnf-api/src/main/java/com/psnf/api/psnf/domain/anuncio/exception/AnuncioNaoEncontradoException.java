package com.psnf.api.psnf.domain.anuncio.exception;

import java.util.UUID;

public class AnuncioNaoEncontradoException extends RuntimeException {

    public AnuncioNaoEncontradoException(UUID id) {
        super("Anúncio não encontrado: " + id);
    }
}
