package com.psnf.api.psnf.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

import java.io.IOException;

/** 401/403 no mesmo formato JSON dos demais erros da API: {"erro": "..."} */
final class RespostasDeSeguranca {

    private RespostasDeSeguranca() {
    }

    static AuthenticationEntryPoint naoAutenticado() {
        return (request, response, ex) -> escrever(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Faça login para continuar");
    }

    static AccessDeniedHandler acessoNegado() {
        return (request, response, ex) -> escrever(response, HttpServletResponse.SC_FORBIDDEN,
                ex instanceof CsrfException
                        ? "Token CSRF ausente ou inválido. Recarregue a página e tente novamente."
                        : "Acesso negado");
    }

    private static void escrever(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
