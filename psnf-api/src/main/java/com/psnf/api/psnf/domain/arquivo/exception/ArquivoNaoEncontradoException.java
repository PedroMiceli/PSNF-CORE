package com.psnf.api.psnf.domain.arquivo.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

public class ArquivoNaoEncontradoException extends RecursoNaoEncontradoException {
    public ArquivoNaoEncontradoException(String nome) {
        super("Arquivo não encontrado: " + nome);
    }
}
