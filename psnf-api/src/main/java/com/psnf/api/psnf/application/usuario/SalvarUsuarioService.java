package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.exception.EmailJaCadastradoException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.SalvarUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.domain.usuario.port.in.mapper.EnderecoCommandMapper;
import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalvarUsuarioService implements SalvarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SenhaEncoderPort senhaEncoderPort;

    @Override
    @Transactional
    public Usuario salvarUsuario(CadastrarUsuarioCommand command) {
        String email = command.email().trim().toLowerCase();
        if (usuarioRepositoryPort.existePorEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }

        Usuario usuario = Usuario.criar(
                command.nome(),
                email,
                senhaEncoderPort.criptografar(command.senha()),
                command.telefone(),
                command.imagem(),
                EnderecoCommandMapper.toDomain(command.enderecos())
        );

        return usuarioRepositoryPort.save(usuario);
    }
}
