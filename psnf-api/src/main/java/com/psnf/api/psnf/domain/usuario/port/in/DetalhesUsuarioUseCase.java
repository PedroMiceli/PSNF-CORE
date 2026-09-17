package com.psnf.api.psnf.domain.usuario.port.in;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.UUID;

public interface DetalhesUsuarioUseCase {

    Usuario detalhesUsuario(UUID usuario);
}
