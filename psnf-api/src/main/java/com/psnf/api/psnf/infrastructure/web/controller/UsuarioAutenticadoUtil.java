package com.psnf.api.psnf.infrastructure.web.controller;

import com.psnf.api.psnf.infrastructure.mock.UsuarioMock;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Solução TEMPORÁRIA até existir autenticação (JWT/Spring Security).
 * <p>
 * Lê o id do usuário logado do header {@code X-Usuario-Id}. Se o header não for enviado,
 * o usuário logado é o {@link UsuarioMock#VENDEDOR}.
 */
public class UsuarioAutenticadoUtil {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

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
        return UsuarioMock.VENDEDOR_ID;
    }
}
