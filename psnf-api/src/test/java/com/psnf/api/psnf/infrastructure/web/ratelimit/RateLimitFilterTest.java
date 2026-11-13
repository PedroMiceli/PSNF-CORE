package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final AtomicLong agora = new AtomicLong(0);

    private BaldesEmMemoria baldes = new BaldesEmMemoria(100_000, agora::get);

    private RateLimitFilter filtro(RateLimitProperties.Limite padrao, RateLimitProperties.Regra... regras) {
        return new RateLimitFilter(new RateLimitProperties(true, padrao, List.of(regras), null, "memoria"), baldes);
    }

    private MockHttpServletResponse chamar(RateLimitFilter f, String metodo, String caminho, String ip) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(metodo, caminho);
        req.setRemoteAddr(ip);
        MockHttpServletResponse res = new MockHttpServletResponse();
        f.doFilter(req, res, new MockFilterChain());
        return res;
    }

    private void avancarSegundos(long s) { agora.addAndGet(TimeUnit.SECONDS.toNanos(s)); }

    @Test
    void permiteARajadaENegaAcimaDaCapacidadeComRetryAfter() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(3, 60)); // 1 ficha por segundo
        for (int i = 0; i < 3; i++) assertThat(chamar(f, "GET", "/anuncios", "1.1.1.1").getStatus()).isEqualTo(200);

        MockHttpServletResponse negada = chamar(f, "GET", "/anuncios", "1.1.1.1");
        assertThat(negada.getStatus()).isEqualTo(429);
        assertThat(negada.getHeader("Retry-After")).isEqualTo("1");
        assertThat(negada.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(negada.getContentAsString()).contains("\"erro\"");
    }

    @Test
    void reabasteceComOTempo() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(2, 60));
        chamar(f, "GET", "/a", "1.1.1.1");
        chamar(f, "GET", "/a", "1.1.1.1");
        assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(429);

        avancarSegundos(1);
        assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(429);
    }

    @Test
    void usuarioLogadoTemBaldeProprioMesmoNoMesmoIp() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(1, 60));
        try {
            logar("11111111-1111-1111-1111-111111111111");
            assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(200);
            assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(429);
            logar("22222222-2222-2222-2222-222222222222"); // colega da mesma empresa, mesmo IP
            assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(200);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    private static void logar(String usuarioId) {
        org.springframework.security.oauth2.jwt.Jwt jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t")
                .header("alg", "HS256").subject(usuarioId).build();
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt));
    }

    @Test
    void cadaIpTemSeuProprioLimite() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(1, 60));
        assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(429);
        assertThat(chamar(f, "GET", "/a", "2.2.2.2").getStatus()).isEqualTo(200);
    }

    @Test
    void regraEspecificaTemBaldeProprioENaoGastaOPadrao() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(2, 60),
                new RateLimitProperties.Regra("upload", "POST", List.of("/arquivos/**"), 1, 60));

        assertThat(chamar(f, "POST", "/arquivos", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(chamar(f, "POST", "/arquivos", "1.1.1.1").getStatus()).isEqualTo(429);
        // outro método no mesmo caminho e demais rotas seguem no padrão, intacto
        assertThat(chamar(f, "GET", "/arquivos/x.png", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(chamar(f, "GET", "/anuncios", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(chamar(f, "GET", "/anuncios", "1.1.1.1").getStatus()).isEqualTo(429);
    }

    @Test
    void preflightDoCorsEDesligadoNaoSaoLimitados() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(1, 60));
        for (int i = 0; i < 5; i++) assertThat(chamar(f, "OPTIONS", "/a", "1.1.1.1").getStatus()).isEqualTo(200);

        RateLimitFilter desligado = new RateLimitFilter(
                new RateLimitProperties(false, new RateLimitProperties.Limite(1, 60), List.of(), null, "memoria"), baldes);
        for (int i = 0; i < 5; i++) assertThat(chamar(desligado, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(200);
    }

    @Test
    void descartaBaldesCheiosParaNaoCrescerAMemoria() throws Exception {
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(2, 60));
        for (int i = 0; i < 50; i++) chamar(f, "GET", "/a", "10.0.0." + i);
        assertThat(baldes.tamanho()).isEqualTo(50);

        avancarSegundos(61); // todos reabasteceram; a próxima requisição dispara a limpeza
        chamar(f, "GET", "/a", "9.9.9.9");
        assertThat(baldes.tamanho()).isEqualTo(1);
    }

    @Test
    void comAMemoriaNoMaximoDeixaPassarEmVezDeCrescer() throws Exception {
        baldes = new BaldesEmMemoria(2, agora::get);
        RateLimitFilter f = filtro(new RateLimitProperties.Limite(1, 60));
        chamar(f, "GET", "/a", "1.1.1.1");
        chamar(f, "GET", "/a", "2.2.2.2");
        // terceiro cliente não cabe: não é limitado, e a memória não cresce
        for (int i = 0; i < 3; i++) assertThat(chamar(f, "GET", "/a", "3.3.3.3").getStatus()).isEqualTo(200);
        assertThat(baldes.tamanho()).isEqualTo(2);
        // quem já tinha balde continua limitado
        assertThat(chamar(f, "GET", "/a", "1.1.1.1").getStatus()).isEqualTo(429);
    }
}
