package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;

public class EmailJaCadastradoException extends RegraDeNegocioException {
    public EmailJaCadastradoException(String email) {
        super("Já existe um usuário cadastrado com o email: " + email);
    }
}
