package com.psnf.api.psnf.infrastructure.persistence.categoria.adapter;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.domain.categoria.port.out.CategoriaRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.categoria.adapter.mapper.CategoriaJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.categoria.repository.CategoriaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository categoriaJpaRepository;

    @Override
    public List<Categoria> buscarTodas() {
        return categoriaJpaRepository.findAllByOrderByNomeAsc().stream().map(CategoriaJpaMapper::toDomain).toList();
    }

    @Override
    public boolean existePorId(UUID categoriaId) {
        return categoriaJpaRepository.existsById(categoriaId);
    }
}
