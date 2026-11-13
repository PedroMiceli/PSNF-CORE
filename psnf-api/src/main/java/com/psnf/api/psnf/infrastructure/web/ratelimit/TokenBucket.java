package com.psnf.api.psnf.infrastructure.web.ratelimit;

import com.psnf.api.psnf.infrastructure.web.ratelimit.ArmazenamentoBaldes.Resultado;

/**
 * Balde de fichas de um cliente, em memória ({@link BaldesEmMemoria}). O reabastecimento é calculado na hora do uso (sem threads de fundo):
 * fichas = min(capacidade, fichas + tempoDecorrido × taxa).
 */
final class TokenBucket {

    private final double capacidade;
    private final double fichasPorNano;
    private double fichas;
    private long atualizadoEm;

    /** @param fichasPorMinuto pode ser fracionário: 0,2 = uma ficha a cada 5 minutos */
    TokenBucket(double capacidade, double fichasPorMinuto, long agoraNanos) {
        this.capacidade = capacidade;
        this.fichasPorNano = fichasPorMinuto / 60_000_000_000d;
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

    /** segundos até haver uma ficha, sem gastar nada (0 = há ficha agora) */
    synchronized long esperaSegundos(long agoraNanos) {
        reabastecer(agoraNanos);
        if (fichas >= 1) return 0;
        return Math.max(1, (long) Math.ceil(Math.ceil((1 - fichas) / fichasPorNano) / 1e9));
    }

    /** gasta uma ficha mesmo que não haja (fica em zero) */
    synchronized void gastar(long agoraNanos) {
        reabastecer(agoraNanos);
        fichas = Math.max(0, fichas - 1);
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
