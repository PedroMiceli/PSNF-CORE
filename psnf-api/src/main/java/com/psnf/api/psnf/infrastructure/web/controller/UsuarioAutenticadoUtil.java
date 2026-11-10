package com.psnf.api.psnf.infrastructure.web.controller;

import com.psnf.api.psnf.domain.shared.exception.NaoAutenticadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

/**
 * Id do usuário logado, tirado do JWT já validado pelo Spring Security (claim "sub").
 * Antes vinha do header X-Usuario-Id, que qualquer um podia preencher com o id de outra pessoa.
 */
public final class UsuarioAutenticadoUtil {

    private UsuarioAutenticadoUtil() {
    }

    public static UUID obterUsuarioLogadoId() {
        return usuarioLogadoId().orElseThrow(() -> new NaoAutenticadoException("Faça login para continuar"));
    }

    public static Optional<UUID> usuarioLogadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(UUID.fromString(jwt.getSubject()));
        }
        return Optional.empty();
    }
}
