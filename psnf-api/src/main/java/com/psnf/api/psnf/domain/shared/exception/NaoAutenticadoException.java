package com.psnf.api.psnf.domain.shared.exception;

/**
 * Falta login, ou ele não vale mais (mapeada para HTTP 401). O front tenta renovar a sessão e,
 * se não der, leva o usuário para a tela de login.
 */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
