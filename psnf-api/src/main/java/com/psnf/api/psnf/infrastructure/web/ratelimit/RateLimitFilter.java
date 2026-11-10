package com.psnf.api.psnf.infrastructure.web.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

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
 * <p>Os baldes ficam em memória, por instância da API: com N instâncias, o limite efetivo é até N vezes
 * maior. Para um limite global, o mesmo algoritmo pode guardar os baldes no Redis.
 */
@Component
// o Spring Security roda em -100 (SecurityProperties.DEFAULT_FILTER_ORDER): este vem logo depois
@Order(-90)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long LIMPEZA_A_CADA = TimeUnit.SECONDS.toNanos(60);

    private record RegraCompilada(String nome, String metodo, List<PathPattern> caminhos, RateLimitProperties.Limite limite) {
        boolean casa(String metodoRequisicao, PathContainer caminho) {
            return (metodo == null || metodo.isBlank() || metodo.equalsIgnoreCase(metodoRequisicao))
                    && caminhos.stream().anyMatch(p -> p.matches(caminho));
        }
    }

    private final RateLimitProperties props;
    private final List<RegraCompilada> regras;
    private final RegraCompilada padrao;
    private final Map<String, TokenBucket> baldes = new ConcurrentHashMap<>();
    private final LongSupplier relogio;
    private final AtomicLong ultimaLimpeza;

    @Autowired
    public RateLimitFilter(RateLimitProperties props) {
        this(props, System::nanoTime);
    }

    RateLimitFilter(RateLimitProperties props, LongSupplier relogio) {
        this.props = props;
        this.relogio = relogio;
        this.ultimaLimpeza = new AtomicLong(relogio.getAsLong());
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
        long agora = relogio.getAsLong();
        limparSeNecessario(agora);

        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        PathContainer path = PathContainer.parsePath(caminho);
        RegraCompilada regra = regras.stream().filter(r -> r.casa(request.getMethod(), path)).findFirst().orElse(padrao);

        String cliente = UsuarioAutenticadoUtil.usuarioLogadoId().map(id -> "u:" + id).orElseGet(() -> "ip:" + request.getRemoteAddr());
        String chave = regra.nome() + '|' + cliente;
        TokenBucket balde = baldes.get(chave);
        if (balde == null) {
            if (baldes.size() >= props.maxClientes()) {
                // proteção de memória: em vez de crescer sem fim (ex.: IPs forjados), deixa passar e avisa
                logger.warn("Rate limit: {} clientes em memória (máximo); requisição de {} não foi limitada",
                        baldes.size(), cliente);
                chain.doFilter(request, response);
                return;
            }
            balde = baldes.computeIfAbsent(chave, k -> new TokenBucket(regra.limite(), agora));
        }

        TokenBucket.Resultado r = balde.tentarConsumir(agora);
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

    /** a cada minuto, descarta baldes cheios: um balde cheio é igual a um novo, então nada muda para o cliente */
    private void limparSeNecessario(long agora) {
        long ultima = ultimaLimpeza.get();
        if (agora - ultima < LIMPEZA_A_CADA || !ultimaLimpeza.compareAndSet(ultima, agora)) return;
        baldes.entrySet().removeIf(e -> e.getValue().cheio(agora));
    }

    int clientesEmMemoria() { return baldes.size(); }
}
