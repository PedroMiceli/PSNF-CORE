package com.psnf.api.psnf.domain.shared.exception;

/**
 * Base para todas as exceções de "recurso não encontrado" do domínio (mapeadas para HTTP 404).
 */
public abstract class RecursoNaoEncontradoException extends RuntimeException {
    protected RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
