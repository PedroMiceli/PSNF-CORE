package com.psnf.api.psnf.infrastructure.web.ratelimit;

/**
 * Balde de fichas de um cliente. O reabastecimento é calculado na hora do uso (sem threads de fundo):
 * fichas = min(capacidade, fichas + tempoDecorrido × taxa).
 */
final class TokenBucket {

    /** resultado de uma tentativa: quantas fichas sobraram e, se negado, em quantos segundos haverá uma */
    record Resultado(boolean permitido, int restantes, long esperarSegundos) {}

    private final double capacidade;
    private final double fichasPorNano;
    private double fichas;
    private long atualizadoEm;

    TokenBucket(RateLimitProperties.Limite limite, long agoraNanos) {
        this.capacidade = limite.capacidade();
        this.fichasPorNano = limite.porMinuto() / 60_000_000_000d;
        this.fichas = capacidade;
        this.atualizadoEm = agoraNanos;
    }

    synchronized Resultado tentarConsumir(long agoraNanos) {
        reabastecer(agoraNanos);
        if (fichas >= 1) {
            fichas -= 1;
            return new Resultado(true, (int) fichas, 0);
        }
        long nanosAteUmaFicha = (long) Math.ceil((1 - fichas) / fichasPorNano);
        return new Resultado(false, 0, Math.max(1, (long) Math.ceil(nanosAteUmaFicha / 1e9)));
    }

    /** cheio de novo: equivale a um balde novo, então pode ser descartado sem mudar o comportamento */
    synchronized boolean cheio(long agoraNanos) {
        reabastecer(agoraNanos);
        return fichas >= capacidade;
    }

    private void reabastecer(long agoraNanos) {
        long decorrido = agoraNanos - atualizadoEm;
        if (decorrido > 0) {
            fichas = Math.min(capacidade, fichas + decorrido * fichasPorNano);
            atualizadoEm = agoraNanos;
        }
    }
}
