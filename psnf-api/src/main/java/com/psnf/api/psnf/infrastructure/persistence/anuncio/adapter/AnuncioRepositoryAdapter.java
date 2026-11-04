package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter;

import com.psnf.api.psnf.domain.anuncio.exception.AnuncioNaoEncontradoException;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.categoria.repository.CategoriaJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnuncioRepositoryAdapter implements AnuncioRepositoryPort {

    private final AnuncioJPARepository anuncioJPARepository;
    private final CategoriaJpaRepository categoriaJpaRepository;
    private final UsuarioJPARepository usuarioJPARepository;

    @Override
    public Anuncio save(Anuncio anuncio) {
        AnuncioJpa entity = AnuncioJpaMapper.toEntity(
                anuncio,
                categoriaJpaRepository.getReferenceById(anuncio.getCategoria()),
                usuarioJPARepository.getReferenceById(anuncio.getVendedor())
        );
        // valores, imagens e variações são salvos em cascata
        return AnuncioJpaMapper.toDomain(anuncioJPARepository.saveAndFlush(entity));
    }

    @Override
    public Anuncio buscarPorId(UUID id) {
        return anuncioJPARepository.findById(id)
                .map(AnuncioJpaMapper::toDomain)
                .orElseThrow(() -> new AnuncioNaoEncontradoException(id));
    }

    @Override
    public List<Anuncio> buscarPorVendedor(UUID vendedorId) {
        return anuncioJPARepository.findByVendedorIdOrderByDataCadastroDesc(vendedorId).stream()
                .map(AnuncioJpaMapper::toDomain)
                .toList();
    }
}
