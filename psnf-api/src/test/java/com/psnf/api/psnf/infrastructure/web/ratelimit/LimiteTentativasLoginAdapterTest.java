package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class LimiteTentativasLoginAdapterTest {

    private final AtomicLong agora = new AtomicLong(0);
    private final LimiteTentativasLoginAdapter limite = new LimiteTentativasLoginAdapter(5, 5, new BaldesEmMemoria(100_000, agora::get));

    private void avancarMinutos(long m) { agora.addAndGet(TimeUnit.MINUTES.toNanos(m)); }

    @Test
    void toleraCincoErrosEDepoisUmaTentativaACadaCincoMinutos() {
        for (int i = 0; i < 4; i++) limite.registrarFalha("a@x.com");
        assertThat(limite.esperaSegundos("a@x.com")).isZero();

        limite.registrarFalha("a@x.com"); // 5ª
        assertThat(limite.esperaSegundos("a@x.com")).isBetween(299L, 300L);

        avancarMinutos(5);
        assertThat(limite.esperaSegundos("a@x.com")).isZero(); // ganhou uma tentativa
        limite.registrarFalha("a@x.com");
        assertThat(limite.esperaSegundos("a@x.com")).isPositive();
    }

    @Test
    void sucessoZeraEOutrosEmailsNaoSaoAfetados() {
        for (int i = 0; i < 5; i++) limite.registrarFalha("a@x.com");
        assertThat(limite.esperaSegundos("b@x.com")).isZero();
        limite.registrarSucesso("a@x.com");
        assertThat(limite.esperaSegundos("a@x.com")).isZero();
    }
}
