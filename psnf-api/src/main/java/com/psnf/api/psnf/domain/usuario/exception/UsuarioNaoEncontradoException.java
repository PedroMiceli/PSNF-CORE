package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class UsuarioNaoEncontradoException extends RecursoNaoEncontradoException {
    public UsuarioNaoEncontradoException(UUID id) {
        super("Usuário não encontrado: " + id);
    }
}
