package com.psnf.api.psnf.domain.autenticacao.exception;

import com.psnf.api.psnf.domain.shared.exception.NaoAutenticadoException;

/**
 * E-mail ou senha errados. A mensagem é a mesma nos dois casos de propósito: dizer "e-mail não
 * cadastrado" permitiria descobrir quem tem conta na plataforma.
 */
public class CredenciaisInvalidasException extends NaoAutenticadoException {
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
