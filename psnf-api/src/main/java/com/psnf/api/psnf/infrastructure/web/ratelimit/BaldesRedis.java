package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Baldes no Redis: todas as instâncias da API enxergam o mesmo balde, então o limite vale para a
 * plataforma inteira e não por instância.
 *
 * <p>Cada operação é UMA chamada ao Redis, que executa o script {@code redis/balde.lua} de forma atômica
 * (duas instâncias nunca gastam a mesma ficha). O balde é um hash {@code f} (fichas) / {@code t} (quando
 * foi atualizado) que expira sozinho quando voltaria a estar cheio.
 *
 * <p><b>Redis fora do ar.</b> A API não para: as requisições passam a ser limitadas pelos baldes em memória
 * desta instância (limite por instância, como era antes do Redis). Para não esperar o tempo limite de
 * conexão a cada requisição, depois de uma falha o Redis fica {@value #PAUSA_SEGUNDOS} s sem ser consultado.
 * A queda e a volta são registradas no log uma vez cada.
 */
public class BaldesRedis implements ArmazenamentoBaldes {

    private static final Logger logger = LoggerFactory.getLogger(BaldesRedis.class);
    static final String PREFIXO = "psnf:balde:";
    private static final int PAUSA_SEGUNDOS = 5;
    private static final long PAUSA_NANOS = TimeUnit.SECONDS.toNanos(PAUSA_SEGUNDOS);

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> SCRIPT = RedisScript.of(new ClassPathResource("redis/balde.lua"), List.class);

    private final StringRedisTemplate redis;
    private final ArmazenamentoBaldes reserva;
    /** 0 = Redis em uso; senão, o instante (nanoTime) em que ele volta a ser consultado */
    private final AtomicLong pausadoAte = new AtomicLong(0);

    public BaldesRedis(StringRedisTemplate redis, ArmazenamentoBaldes reserva) {
        this.redis = redis;
        this.reserva = reserva;
    }

    @Override
    public Resultado tentarConsumir(String chave, double capacidade, double fichasPorMinuto) {
        return comReserva(() -> executar(chave, capacidade, fichasPorMinuto, "consumir"),
                () -> reserva.tentarConsumir(chave, capacidade, fichasPorMinuto));
    }

    @Override
    public long esperaSegundos(String chave, double capacidade, double fichasPorMinuto) {
        return comReserva(() -> executar(chave, capacidade, fichasPorMinuto, "espiar").esperarSegundos(),
                () -> reserva.esperaSegundos(chave, capacidade, fichasPorMinuto));
    }

    @Override
    public void gastar(String chave, double capacidade, double fichasPorMinuto) {
        comReserva(() -> executar(chave, capacidade, fichasPorMinuto, "gastar"),
                () -> { reserva.gastar(chave, capacidade, fichasPorMinuto); return null; });
    }

    @Override
    public void remover(String chave) {
        reserva.remover(chave); // sempre: o balde pode ter sido criado na reserva durante uma queda
        comReserva(() -> redis.delete(PREFIXO + chave), () -> null);
    }

    private Resultado executar(String chave, double capacidade, double fichasPorMinuto, String operacao) {
        List<?> r = redis.execute(SCRIPT, List.of(PREFIXO + chave),
                Double.toString(capacidade), Double.toString(fichasPorMinuto), operacao);
        return new Resultado(numero(r.get(0)) == 1, (int) numero(r.get(1)), numero(r.get(2)));
    }

    private static long numero(Object o) {
        return ((Number) o).longValue();
    }

    private <T> T comReserva(Supplier<T> noRedis, Supplier<T> naReserva) {
        long ate = pausadoAte.get();
        if (ate != 0 && System.nanoTime() - ate < 0) return naReserva.get();
        try {
            T resultado = noRedis.get();
            if (ate != 0 && pausadoAte.compareAndSet(ate, 0)) {
                logger.info("Rate limit: Redis voltou; os limites são globais de novo");
            }
            return resultado;
        } catch (RuntimeException e) {
            // nanoTime pode ser 0 ou negativo: garante um valor diferente de 0 (que significa "em uso")
            long proxima = System.nanoTime() + PAUSA_NANOS;
            if (pausadoAte.getAndSet(proxima == 0 ? 1 : proxima) == 0) {
                logger.warn("Rate limit: Redis indisponível ({}). Usando os baldes em memória desta instância "
                        + "até ele voltar (nova tentativa a cada {} s)", e.toString(), PAUSA_SEGUNDOS);
            }
            return naReserva.get();
        }
    }
}
