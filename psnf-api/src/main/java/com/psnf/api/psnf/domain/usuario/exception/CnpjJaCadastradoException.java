package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;

/** Cada CNPJ pertence a uma única conta (HTTP 422). */
public class CnpjJaCadastradoException extends RegraDeNegocioException {
    public CnpjJaCadastradoException() {
        super("Este CNPJ já está cadastrado em outra conta");
    }
}
