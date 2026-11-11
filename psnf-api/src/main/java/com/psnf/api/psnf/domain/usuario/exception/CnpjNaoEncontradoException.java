package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

public class CnpjNaoEncontradoException extends RecursoNaoEncontradoException {
    public CnpjNaoEncontradoException() {
        super("CNPJ não encontrado na base da Receita Federal. Se ele é recente, você pode salvar mesmo assim.");
    }
}
