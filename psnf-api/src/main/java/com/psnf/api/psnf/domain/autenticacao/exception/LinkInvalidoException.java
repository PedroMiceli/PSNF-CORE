package com.psnf.api.psnf.domain.autenticacao.exception;

import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;

/** Link de confirmação ou de redefinição vencido, já usado ou inexistente (HTTP 422). */
public class LinkInvalidoException extends RegraDeNegocioException {
    public LinkInvalidoException() {
        super("Este link é inválido ou expirou. Peça um novo.");
    }
}
