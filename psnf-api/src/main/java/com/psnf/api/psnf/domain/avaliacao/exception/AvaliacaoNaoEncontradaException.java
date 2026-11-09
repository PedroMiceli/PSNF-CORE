package com.psnf.api.psnf.domain.avaliacao.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

public class AvaliacaoNaoEncontradaException extends RecursoNaoEncontradoException {
    public AvaliacaoNaoEncontradaException() {
        super("Você ainda não avaliou este produto");
    }
}
