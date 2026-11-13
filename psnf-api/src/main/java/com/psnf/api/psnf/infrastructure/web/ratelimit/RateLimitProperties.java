package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Limite de requisições por cliente (prefixo psnf.rate-limit no application.properties).
 *
 * <p>Cada cliente tem um "balde de fichas" (token bucket): cabe {@code capacidade} fichas, cada requisição
 * gasta uma e o balde se reabastece em {@code porMinuto} fichas por minuto. Assim, rajadas curtas (abrir
 * uma página que faz várias chamadas) passam, mas um ritmo contínuo acima do limite recebe HTTP 429.
 *
 * <p>{@code padrao} vale para todas as rotas. {@code regras} são exceções, avaliadas na ordem: a primeira
 * que casar com o método e o caminho é usada, com um balde próprio (não gasta o balde padrão).
 *
 * <p>{@code armazenamento}: "redis" (padrão: os baldes ficam no Redis e o limite vale para todas as
 * instâncias juntas) ou "memoria" (baldes por instância, sem depender do Redis). {@code maxClientes}
 * limita só os baldes em memória; no Redis, cada balde expira sozinho.
 */
@ConfigurationProperties(prefix = "psnf.rate-limit")
public record RateLimitProperties(
        Boolean habilitado,
        Limite padrao,
        List<Regra> regras,
        Integer maxClientes,
        String armazenamento
) {
    public RateLimitProperties {
        if (habilitado == null) habilitado = true;
        if (padrao == null) padrao = new Limite(100, 300);
        if (regras == null) regras = List.of();
        if (maxClientes == null || maxClientes <= 0) maxClientes = 100_000;
        armazenamento = armazenamento == null || armazenamento.isBlank() ? "redis" : armazenamento.trim().toLowerCase();
        if (!armazenamento.equals("redis") && !armazenamento.equals("memoria"))
            throw new IllegalArgumentException("psnf.rate-limit.armazenamento deve ser \"redis\" ou \"memoria\"");
    }

    public boolean usaRedis() { return armazenamento.equals("redis"); }

    public record Limite(int capacidade, int porMinuto) {
        public Limite {
            if (capacidade <= 0 || porMinuto <= 0)
                throw new IllegalArgumentException("psnf.rate-limit: capacidade e por-minuto devem ser maiores que zero");
        }
    }

    /**
     * @param nome     identifica a regra no log e separa os baldes
     * @param metodo   GET, POST... (vazio = qualquer método)
     * @param caminhos padrões de caminho, ex.: /arquivos/**
     */
    public record Regra(String nome, String metodo, List<String> caminhos, int capacidade, int porMinuto) {
        public Regra {
            if (nome == null || nome.isBlank()) throw new IllegalArgumentException("psnf.rate-limit: regra sem nome");
            if (caminhos == null || caminhos.isEmpty())
                throw new IllegalArgumentException("psnf.rate-limit: regra " + nome + " sem caminhos");
            new Limite(capacidade, porMinuto); // valida
        }

        Limite limite() { return new Limite(capacidade, porMinuto); }
    }
}
