package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class EnderecoNaoEncontradoException extends RecursoNaoEncontradoException {
    public EnderecoNaoEncontradoException(UUID id) {
        super("Endereço não encontrado: " + id);
    }
}
