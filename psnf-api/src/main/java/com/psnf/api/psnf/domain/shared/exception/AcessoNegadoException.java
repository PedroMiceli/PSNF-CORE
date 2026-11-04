package com.psnf.api.psnf.domain.shared.exception;

/**
 * O usuário logado não tem permissão para executar a operação (mapeada para HTTP 403).
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
