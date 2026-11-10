package com.psnf.api.psnf.domain.autenticacao.model;

import java.time.Duration;

/** Links de uso único enviados por e-mail e por quanto tempo valem. */
public enum TipoTokenUsuario {
    CONFIRMACAO_EMAIL(Duration.ofHours(24)),
    /** curto: quem tiver acesso ao e-mail por um instante não ganha uma janela longa para trocar a senha */
    REDEFINICAO_SENHA(Duration.ofHours(1));

    private final Duration validade;

    TipoTokenUsuario(Duration validade) { this.validade = validade; }

    public Duration validade() { return validade; }
}
