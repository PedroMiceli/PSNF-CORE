package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Baldes na memória desta instância da API. Com N instâncias, o limite efetivo é até N vezes maior:
 * serve para desenvolvimento, para uma instância só e como reserva do {@link BaldesRedis} quando o Redis
 * está fora do ar.
 */
public class BaldesEmMemoria implements ArmazenamentoBaldes {

    private static final Logger logger = LoggerFactory.getLogger(BaldesEmMemoria.class);
    private static final long LIMPEZA_A_CADA = TimeUnit.SECONDS.toNanos(60);

    private final int maxChaves;
    private final LongSupplier relogio;
    private final Map<String, TokenBucket> baldes = new ConcurrentHashMap<>();
    private final AtomicLong ultimaLimpeza;

    public BaldesEmMemoria(int maxChaves) {
        this(maxChaves, System::nanoTime);
    }

    BaldesEmMemoria(int maxChaves, LongSupplier relogio) {
        this.maxChaves = maxChaves;
        this.relogio = relogio;
        this.ultimaLimpeza = new AtomicLong(relogio.getAsLong());
    }

    @Override
    public Resultado tentarConsumir(String chave, double capacidade, double fichasPorMinuto) {
        long agora = relogio.getAsLong();
        TokenBucket balde = balde(chave, capacidade, fichasPorMinuto, agora);
        if (balde == null) {
            // proteção de memória: em vez de crescer sem fim (ex.: IPs forjados), deixa passar e avisa
            logger.warn("Rate limit: {} baldes em memória (máximo); {} não foi limitado", baldes.size(), chave);
            return new Resultado(true, (int) capacidade, 0);
        }
        return balde.tentarConsumir(agora);
    }

    @Override
    public long esperaSegundos(String chave, double capacidade, double fichasPorMinuto) {
        TokenBucket balde = baldes.get(chave);
        return balde == null ? 0 : balde.esperaSegundos(relogio.getAsLong());
    }

    @Override
    public void gastar(String chave, double capacidade, double fichasPorMinuto) {
        long agora = relogio.getAsLong();
        TokenBucket balde = balde(chave, capacidade, fichasPorMinuto, agora);
        if (balde != null) balde.gastar(agora);
    }

    @Override
    public void remover(String chave) {
        baldes.remove(chave);
    }

    /** o balde da chave, criado cheio se não existir; null se a memória chegou ao máximo de chaves */
    private TokenBucket balde(String chave, double capacidade, double fichasPorMinuto, long agora) {
        limparSeNecessario(agora);
        TokenBucket balde = baldes.get(chave);
        if (balde != null) return balde;
        if (baldes.size() >= maxChaves) return null;
        return baldes.computeIfAbsent(chave, k -> new TokenBucket(capacidade, fichasPorMinuto, agora));
    }

    /** a cada minuto, descarta baldes cheios: um balde cheio é igual a um novo, então nada muda para o cliente */
    private void limparSeNecessario(long agora) {
        long ultima = ultimaLimpeza.get();
        if (agora - ultima < LIMPEZA_A_CADA || !ultimaLimpeza.compareAndSet(ultima, agora)) return;
        baldes.entrySet().removeIf(e -> e.getValue().cheio(agora));
    }

    int tamanho() { return baldes.size(); }
}
