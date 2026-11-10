package com.psnf.api.psnf.domain.autenticacao.exception;

/**
 * Senha certa, mas o e-mail ainda não foi confirmado (HTTP 403, código EMAIL_NAO_CONFIRMADO).
 * Só aparece depois da senha conferida: não serve para descobrir quem tem conta.
 */
public class EmailNaoConfirmadoException extends RuntimeException {
    public EmailNaoConfirmadoException() {
        super("Confirme seu e-mail para entrar: enviamos um link no cadastro.");
    }
}
