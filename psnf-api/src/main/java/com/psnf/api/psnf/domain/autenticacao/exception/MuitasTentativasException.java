package com.psnf.api.psnf.domain.autenticacao.exception;

/** Senhas erradas demais para este e-mail (HTTP 429 com Retry-After). */
public class MuitasTentativasException extends RuntimeException {

    private final long esperaSegundos;

    public MuitasTentativasException(long esperaSegundos) {
        super("Muitas tentativas de login para este e-mail. Tente novamente em "
                + (esperaSegundos >= 60 ? (esperaSegundos + 59) / 60 + " minuto(s)" : esperaSegundos + " s")
                + " ou redefina sua senha.");
        this.esperaSegundos = esperaSegundos;
    }

    public long getEsperaSegundos() { return esperaSegundos; }
}
