package com.psnf.api.psnf.domain.negociacao.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class NegociacaoNaoEncontradaException extends RecursoNaoEncontradoException {
    public NegociacaoNaoEncontradaException(UUID id) {
        super("Negociação não encontrada: " + id);
    }
}
