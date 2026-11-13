package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testa os baldes em um Redis de verdade (o script Lua só existe lá). Usa PSNF_REDIS_HOST/PSNF_REDIS_PORT
 * (padrão localhost:6379). Sem Redis no ar, os testes que dependem dele são pulados; os da queda do Redis
 * rodam sempre.
 */
class BaldesRedisTest {

    private static LettuceConnectionFactory fabrica;
    private static StringRedisTemplate redis;
    private static boolean redisNoAr;

    private final String prefixo = "teste:" + UUID.randomUUID() + ":";
    private BaldesRedis baldes;

    private static LettuceConnectionFactory fabrica(String host, int porta) {
        LettuceConnectionFactory f = new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, porta),
                LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(500)).build());
        f.afterPropertiesSet();
        return f;
    }

    @BeforeAll
    static void conectar() {
        String host = System.getenv().getOrDefault("PSNF_REDIS_HOST", "localhost");
        int porta = Integer.parseInt(System.getenv().getOrDefault("PSNF_REDIS_PORT", "6379"));
        fabrica = fabrica(host, porta);
        redis = new StringRedisTemplate(fabrica);
        try {
            redisNoAr = "PONG".equals(redis.execute((org.springframework.data.redis.core.RedisCallback<String>) c -> c.ping()));
        } catch (RuntimeException e) {
            redisNoAr = false;
        }
    }

    @AfterAll
    static void desconectar() {
        if (redisNoAr) {
            Set<String> chaves = redis.keys(BaldesRedis.PREFIXO + "teste:*");
            if (chaves != null && !chaves.isEmpty()) redis.delete(chaves);
        }
        fabrica.destroy();
    }

    @BeforeEach
    void criar() {
        baldes = new BaldesRedis(redis, new BaldesEmMemoria(1000));
    }

    @Test
    void permiteARajadaENegaDepoisComOTempoDeEspera() {
        assumeTrue(redisNoAr, "Redis fora do ar");
        String chave = prefixo + "rajada";
        assertThat(baldes.tentarConsumir(chave, 3, 60).restantes()).isEqualTo(2);
        assertThat(baldes.tentarConsumir(chave, 3, 60).permitido()).isTrue();
        assertThat(baldes.tentarConsumir(chave, 3, 60).restantes()).isZero();

        ArmazenamentoBaldes.Resultado negada = baldes.tentarConsumir(chave, 3, 60);
        assertThat(negada.permitido()).isFalse();
        assertThat(negada.restantes()).isZero();
        assertThat(negada.esperarSegundos()).isEqualTo(1); // 60 por minuto = 1 ficha por segundo
    }

    @Test
    void reabasteceComOTempoDoRedis() throws Exception {
        assumeTrue(redisNoAr, "Redis fora do ar");
        String chave = prefixo + "reabastece";
        baldes.tentarConsumir(chave, 1, 600); // 10 fichas por segundo
        assertThat(baldes.tentarConsumir(chave, 1, 600).permitido()).isFalse();
        Thread.sleep(150);
        assertThat(baldes.tentarConsumir(chave, 1, 600).permitido()).isTrue();
    }

    @Test
    void duasInstanciasDividemOMesmoBalde() {
        assumeTrue(redisNoAr, "Redis fora do ar");
        // outra "instância da API": outro objeto, outra reserva em memória, mesmo Redis
        BaldesRedis outraInstancia = new BaldesRedis(redis, new BaldesEmMemoria(1000));
        String chave = prefixo + "global";
        assertThat(baldes.tentarConsumir(chave, 2, 1).permitido()).isTrue();
        assertThat(outraInstancia.tentarConsumir(chave, 2, 1).permitido()).isTrue();
        assertThat(baldes.tentarConsumir(chave, 2, 1).permitido()).isFalse();
        assertThat(outraInstancia.tentarConsumir(chave, 2, 1).permitido()).isFalse();
    }

    @Test
    void chamadasSimultaneasNuncaPassamDaCapacidade() throws Exception {
        assumeTrue(redisNoAr, "Redis fora do ar");
        String chave = prefixo + "concorrencia";
        int chamadas = 200, capacidade = 50;
        AtomicInteger permitidas = new AtomicInteger();
        CountDownLatch largada = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < chamadas; i++) {
                pool.submit(() -> {
                    largada.await();
                    // 0,001 ficha por minuto: nada reabastece durante o teste
                    if (baldes.tentarConsumir(chave, capacidade, 0.001).permitido()) permitidas.incrementAndGet();
                    return null;
                });
            }
            largada.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(permitidas.get()).isEqualTo(capacidade);
    }

    @Test
    void baldeExpiraSozinhoQuandoVoltariaAEstarCheio() {
        assumeTrue(redisNoAr, "Redis fora do ar");
        String chave = prefixo + "expira";
        baldes.tentarConsumir(chave, 10, 60); // falta 1 ficha: enche em 1 s (+1 s de folga)
        Long ttl = redis.getExpire(BaldesRedis.PREFIXO + chave, TimeUnit.MILLISECONDS);
        assertThat(ttl).isBetween(1L, 2000L);
    }

    @Test
    void freioDeLoginGastaSoNasFalhasEZeraNoSucesso() {
        assumeTrue(redisNoAr, "Redis fora do ar");
        LimiteTentativasLoginAdapter limite = new LimiteTentativasLoginAdapter(5, 5, baldes);
        String email = prefixo + "a@x.com";
        assertThat(limite.esperaSegundos(email)).isZero();
        for (int i = 0; i < 4; i++) limite.registrarFalha(email);
        assertThat(limite.esperaSegundos(email)).isZero();
        limite.registrarFalha(email); // 5ª
        assertThat(limite.esperaSegundos(email)).isBetween(295L, 300L);
        // consultar não gasta nem cria balde para outro e-mail
        assertThat(limite.esperaSegundos(prefixo + "b@x.com")).isZero();
        assertThat(redis.hasKey(BaldesRedis.PREFIXO + "login:" + prefixo + "b@x.com")).isFalse();

        limite.registrarSucesso(email);
        assertThat(limite.esperaSegundos(email)).isZero();
    }

    @Test
    void comORedisForaDoArLimitaPelaMemoriaDaInstancia() {
        // porta em que não há Redis: toda chamada falha
        LettuceConnectionFactory semRedis = fabrica("localhost", 1);
        try {
            BaldesRedis semConexao = new BaldesRedis(new StringRedisTemplate(semRedis), new BaldesEmMemoria(1000));
            assertThat(semConexao.tentarConsumir("x", 2, 1).permitido()).isTrue();
            assertThat(semConexao.tentarConsumir("x", 2, 1).permitido()).isTrue();
            assertThat(semConexao.tentarConsumir("x", 2, 1).permitido()).isFalse(); // continua limitando
            semConexao.gastar("y", 1, 1);
            assertThat(semConexao.esperaSegundos("y", 1, 1)).isPositive();
            semConexao.remover("y");
            assertThat(semConexao.esperaSegundos("y", 1, 1)).isZero();
        } finally {
            semRedis.destroy();
        }
    }
}
