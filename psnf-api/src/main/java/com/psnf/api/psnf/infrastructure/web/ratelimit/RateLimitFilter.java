package com.psnf.api.psnf.infrastructure.web.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.List;

/**
 * Rate limit em todas as rotas. Roda logo DEPOIS do Spring Security (que só valida o JWT, sem banco) e
 * antes dos controllers, então uma rajada abusiva não chega a abrir transação nem conexão com o banco.
 *
 * <p>Chave do balde:
 * <ul>
 *   <li>usuário logado: o id dele (o "sub" do JWT, que ninguém consegue forjar). Vários funcionários
 *       atrás do mesmo IP de uma empresa não dividem o limite.</li>
 *   <li>sem login (login, cadastro, renovação): o IP. Atrás de load balancer/proxy, configure
 *       server.forward-headers-strategy para o IP real do cliente ser usado.</li>
 * </ul>
 * Requisições sem login para rotas protegidas param no Spring Security (401) antes de chegar aqui.
 *
 * <p>Os baldes ficam no {@link ArmazenamentoBaldes}: no Redis (padrão), o limite vale para todas as
 * instâncias da API juntas; em memória, vale por instância.
 */
@Component
// o Spring Security roda em -100 (SecurityProperties.DEFAULT_FILTER_ORDER): este vem logo depois
@Order(-90)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private record RegraCompilada(String nome, String metodo, List<PathPattern> caminhos, RateLimitProperties.Limite limite) {
        boolean casa(String metodoRequisicao, PathContainer caminho) {
            return (metodo == null || metodo.isBlank() || metodo.equalsIgnoreCase(metodoRequisicao))
                    && caminhos.stream().anyMatch(p -> p.matches(caminho));
        }
    }

    private final RateLimitProperties props;
    private final List<RegraCompilada> regras;
    private final RegraCompilada padrao;
    private final ArmazenamentoBaldes baldes;

    public RateLimitFilter(RateLimitProperties props, ArmazenamentoBaldes baldes) {
        this.props = props;
        this.baldes = baldes;
        PathPatternParser parser = PathPatternParser.defaultInstance;
        this.regras = props.regras().stream()
                .map(r -> new RegraCompilada(r.nome(), r.metodo(), r.caminhos().stream().map(parser::parse).toList(), r.limite()))
                .toList();
        this.padrao = new RegraCompilada("padrao", null, List.of(), props.padrao());
        if (props.habilitado()) {
            logger.info("Rate limit ativo: padrão {} req/min (rajada {}), {} regra(s) específica(s)",
                    props.padrao().porMinuto(), props.padrao().capacidade(), regras.size());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // preflight do CORS não conta: senão cada chamada de outro domínio gastaria duas fichas
        return !props.habilitado() || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        PathContainer path = PathContainer.parsePath(caminho);
        RegraCompilada regra = regras.stream().filter(r -> r.casa(request.getMethod(), path)).findFirst().orElse(padrao);

        String cliente = UsuarioAutenticadoUtil.usuarioLogadoId().map(id -> "u:" + id).orElseGet(() -> "ip:" + request.getRemoteAddr());
        String chave = "rl:" + regra.nome() + '|' + cliente;
        ArmazenamentoBaldes.Resultado r = baldes.tentarConsumir(chave, regra.limite().capacidade(), regra.limite().porMinuto());
        response.setHeader("X-RateLimit-Limit", String.valueOf(regra.limite().porMinuto()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(r.restantes()));
        if (r.permitido()) {
            chain.doFilter(request, response);
            return;
        }

        logger.debug("Rate limit: {} {} de {} negado (regra {})", request.getMethod(), caminho, cliente, regra.nome());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(r.esperarSegundos()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"Muitas requisições em pouco tempo. Tente novamente em "
                + r.esperarSegundos() + " s.\"}");
    }
}
