package com.psnf.api.psnf.domain.autenticacao.exception;

import com.psnf.api.psnf.domain.shared.exception.NaoAutenticadoException;

/** Refresh token ausente, expirado, revogado ou desconhecido: é preciso entrar de novo. */
public class SessaoInvalidaException extends NaoAutenticadoException {
    public SessaoInvalidaException() {
        super("Sua sessão expirou. Entre novamente.");
    }
}
