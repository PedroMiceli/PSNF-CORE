package com.psnf.api.psnf.domain.usuario.port.out;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.UUID;

public interface UsuarioRepositoryPort {

    Usuario save(Usuario usuario);

    Usuario detalhesUsuario(UUID usuario);
}
