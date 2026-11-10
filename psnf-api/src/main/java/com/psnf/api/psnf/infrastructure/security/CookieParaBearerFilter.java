package com.psnf.api.psnf.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

/**
 * Leva o access token do cookie psnf_access para o header Authorization, para o resource server do
 * Spring validar o JWT como se tivesse vindo no header.
 *
 * <p>Fica DEPOIS do filtro de CSRF de propósito: o Spring dispensa CSRF quando a requisição traz o token
 * no header (porque outro site não consegue pôr esse header). Se o cookie fosse lido direto como bearer,
 * todo pedido com cookie escaparia da checagem de CSRF, justamente o caso que ela precisa cobrir.
 *
 * <p>Em login, renovação e logout o cookie é ignorado: um access token vencido não pode impedir
 * justamente as rotas que resolvem o vencimento.
 */
class CookieParaBearerFilter extends OncePerRequestFilter {

    private static final Set<String> SEM_TOKEN =
            Set.of("/auth/login", "/auth/token", "/auth/refresh", "/auth/logout", "/auth/csrf");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        String token = CookiesAutenticacao.ler(request, CookiesAutenticacao.ACCESS);
        if (token == null || request.getHeader(HttpHeaders.AUTHORIZATION) != null || SEM_TOKEN.contains(caminho)) {
            chain.doFilter(request, response);
            return;
        }
        String bearer = "Bearer " + token;
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name) ? bearer : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)
                        ? Collections.enumeration(java.util.List.of(bearer)) : super.getHeaders(name);
            }
        }, response);
    }
}
