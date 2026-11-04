package com.psnf.api.psnf.infrastructure.web.controller;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Solução TEMPORÁRIA até existir autenticação (JWT/Spring Security).
 * <p>
 * Lê o id do usuário logado do header {@code X-Usuario-Id}. Se o header não for enviado,
 * usa um usuário fixo — assim dá para testar fluxos com comprador e vendedor diferentes.
 */
public class UsuarioAutenticadoUtil {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private static final UUID USUARIO_FIXO_TEMPORARIO = UUID.fromString("3c288400-f58f-4d28-905c-f7dba388d6c9");

    public static UUID obterUsuarioLogadoId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String header = attrs.getRequest().getHeader(HEADER_USUARIO);
            if (header != null && !header.isBlank()) {
                try {
                    return UUID.fromString(header.trim());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Header " + HEADER_USUARIO + " inválido: " + header);
                }
            }
        }
        return USUARIO_FIXO_TEMPORARIO;
    }
}
