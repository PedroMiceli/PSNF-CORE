package com.psnf.api.psnf.domain.anuncio.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class AnuncioNaoEncontradoException extends RecursoNaoEncontradoException {

    public AnuncioNaoEncontradoException(UUID id) {
        super("Anúncio não encontrado: " + id);
    }
}
