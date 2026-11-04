package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.GerenciarEnderecoUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.command.EnderecoCommand;
import com.psnf.api.psnf.domain.usuario.port.in.mapper.EnderecoCommandMapper;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GerenciarEnderecoService implements GerenciarEnderecoUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional
    public Usuario adicionarEndereco(UUID usuarioId, EnderecoCommand endereco) {
        Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId);
        usuario.adicionarEndereco(EnderecoCommandMapper.toDomain(endereco));
        return usuarioRepositoryPort.save(usuario);
    }

    @Override
    @Transactional
    public Usuario removerEndereco(UUID usuarioId, UUID enderecoId) {
        Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId);
        usuario.removerEndereco(enderecoId);
        return usuarioRepositoryPort.save(usuario);
    }
}
