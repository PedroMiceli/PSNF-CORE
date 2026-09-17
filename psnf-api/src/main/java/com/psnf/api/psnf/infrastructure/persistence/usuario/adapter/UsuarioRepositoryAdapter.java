package com.psnf.api.psnf.infrastructure.persistence.usuario.adapter;

import com.psnf.api.psnf.domain.usuario.exception.UsuarioNaoEncontradoException;
import com.psnf.api.psnf.domain.usuario.model.Endereco;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper.EnderecoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper.UsuarioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.EnderecoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.EnderecoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJPARepository usuarioJPARepository;

    @Override
    public Usuario save(Usuario usuario) {

        UsuarioJpa usuarioJpa = UsuarioJpaMapper.toEntity(usuario);

        usuario.getEnderecos().stream()
                .map(endereco -> EnderecoJpaMapper.toEntity(endereco, usuarioJpa))
                .forEach(usuarioJpa.getEnderecos()::add);

        UsuarioJpa usuarioSalvo = usuarioJPARepository.saveAndFlush(usuarioJpa);

        return UsuarioJpaMapper.toDomain(usuarioSalvo);
    }

    @Override
    public Usuario detalhesUsuario(UUID usuario) {
        return usuarioJPARepository.findById(usuario).map(UsuarioJpaMapper::toDomain).orElseThrow(() -> new UsuarioNaoEncontradoException(usuario));
    }
}
