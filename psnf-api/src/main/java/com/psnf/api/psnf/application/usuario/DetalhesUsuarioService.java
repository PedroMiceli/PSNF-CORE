package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.DetalhesUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DetalhesUsuarioService implements DetalhesUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Usuario detalhesUsuario(UUID usuario) {
        return usuarioRepositoryPort.buscarPorId(usuario);
    }
}
