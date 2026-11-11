package com.psnf.api.psnf.infrastructure.persistence.usuario.adapter;

import com.psnf.api.psnf.domain.usuario.exception.UsuarioNaoEncontradoException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper.UsuarioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJPARepository usuarioJPARepository;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioJpa usuarioSalvo = usuarioJPARepository.saveAndFlush(UsuarioJpaMapper.toEntity(usuario));
        return UsuarioJpaMapper.toDomain(usuarioSalvo);
    }

    @Override
    public Map<UUID, Usuario> buscarPorIds(Collection<UUID> ids) {
        if (ids.isEmpty()) return Map.of();
        return usuarioJPARepository.findAllById(ids).stream().map(UsuarioJpaMapper::toDomain)
                .collect(Collectors.toMap(Usuario::getId, u -> u));
    }

    @Override
    public Usuario buscarPorId(UUID usuarioId) {
        return usuarioJPARepository.findById(usuarioId)
                .map(UsuarioJpaMapper::toDomain)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioJPARepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public java.util.Optional<Usuario> buscarPorEmail(String email) {
        return usuarioJPARepository.findByEmailIgnoreCase(email.trim()).map(UsuarioJpaMapper::toDomain);
    }

    @Override
    public boolean cnpjEmUsoPorOutro(String cnpj, UUID exceto) {
        return cnpj != null && usuarioJPARepository.cnpjEmUso(cnpj, exceto);
    }
}
