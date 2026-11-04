package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.UUID;

public record UsuarioResumoResponse(
        UUID id,
        String nome,
        String email,
        String telefone
) {
    public static UsuarioResumoResponse from(Usuario usuario) {
        return new UsuarioResumoResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone());
    }
}
