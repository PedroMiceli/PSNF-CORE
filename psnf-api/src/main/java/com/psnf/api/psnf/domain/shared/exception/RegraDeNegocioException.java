package com.psnf.api.psnf.domain.shared.exception;

/**
 * Violação de uma regra de negócio (mapeada para HTTP 422).
 */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
