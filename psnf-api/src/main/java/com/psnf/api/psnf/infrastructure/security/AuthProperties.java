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
 * @param cookieSeguro    atributo Secure dos cookies (só trafegam em HTTPS): "true" sempre, "false" nunca, "auto"
 *                        conforme a requisição (HTTPS: Secure; HTTP: sem). Com Secure, o navegador DESCARTA o
 *                        cookie recebido por HTTP (exceto o Chrome em localhost): o login "funciona" e a sessão some.
 *                        Atrás de proxy que termina o TLS, "auto" depende de server.forward-headers-strategy.
 * @param caminhoRefresh  Path do cookie de refresh: ele só é enviado para as rotas de autenticação, como o
 *                        navegador as vê (com o prefixo /api do proxy do front).
 */
@ConfigurationProperties(prefix = "psnf.auth")
public record AuthProperties(String jwtSegredo, Duration accessValidade, Duration refreshValidade,
                             String cookieSeguro, String caminhoRefresh) {
    public AuthProperties {
        if (accessValidade == null) accessValidade = Duration.ofMinutes(15);
        if (refreshValidade == null) refreshValidade = Duration.ofDays(30);
        if (cookieSeguro == null || cookieSeguro.isBlank()) cookieSeguro = "auto";
        cookieSeguro = cookieSeguro.trim().toLowerCase();
        if (!java.util.Set.of("true", "false", "auto").contains(cookieSeguro)) {
            throw new IllegalStateException("psnf.auth.cookie-seguro deve ser true, false ou auto");
        }
        if (caminhoRefresh == null || caminhoRefresh.isBlank()) caminhoRefresh = "/api/auth";
    }

    /** Secure nesta resposta? */
    public boolean cookieSeguroPara(jakarta.servlet.http.HttpServletRequest request) {
        return switch (cookieSeguro) {
            case "true" -> true;
            case "false" -> false;
            default -> request.isSecure();
        };
    }
}
