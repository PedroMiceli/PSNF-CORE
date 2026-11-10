package com.psnf.api.psnf.domain.autenticacao.model;

/** Uma mensagem de e-mail pronta: texto puro e a mesma mensagem em HTML. */
public record Email(String para, String assunto, String texto, String html) {
}
