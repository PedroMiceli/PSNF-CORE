package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.model.Endereco;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.SalvarUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SalvarUsuarioService implements SalvarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional
    public Usuario salvarUsuario(CadastrarUsuarioCommand usuario) {

        Usuario usuarioDomain = Usuario.criar(
                LocalDateTime.now(),
                null,
                null,
                usuario.nome(),
                usuario.email(),
                usuario.senha(),
                null,
                null,
                usuario.telefone(),
                null
                );

        return usuarioRepositoryPort.save(usuarioDomain);
    }
}
