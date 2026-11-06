package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

public class CepNaoEncontradoException extends RecursoNaoEncontradoException {
    public CepNaoEncontradoException(String cep) {
        super("CEP não encontrado: " + cep);
    }
}
