package com.psnf.api.psnf.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Autenticação (prefixo psnf.auth no application.properties).
 *
 * @param jwtSegredo      chave HMAC do JWT, com pelo menos 32 caracteres. A MESMA em todas as instâncias
 *                        (senão um token emitido por uma não vale na outra). Vazia: gera uma aleatória ao
 *                        iniciar (só para desenvolvimento: todo restart desloga todo mundo).
 * @param accessValidade  vida do access token (JWT). Curta: ele não pode ser revogado antes de expirar.
 * @param refreshValidade vida da sessão sem uso; cada renovação começa a contar de novo.
 * @param cookieSeguro    atributo Secure dos cookies (só HTTPS). Navegadores aceitam em http://localhost.
 * @param caminhoRefresh  Path do cookie de refresh: ele só é enviado para as rotas de autenticação, como o
 *                        navegador as vê (com o prefixo /api do proxy do front).
 */
@ConfigurationProperties(prefix = "psnf.auth")
public record AuthProperties(String jwtSegredo, Duration accessValidade, Duration refreshValidade,
                             Boolean cookieSeguro, String caminhoRefresh) {
    public AuthProperties {
        if (accessValidade == null) accessValidade = Duration.ofMinutes(15);
        if (refreshValidade == null) refreshValidade = Duration.ofDays(30);
        if (cookieSeguro == null) cookieSeguro = true;
        if (caminhoRefresh == null || caminhoRefresh.isBlank()) caminhoRefresh = "/api/auth";
    }
}
