package com.psnf.api.psnf.domain.categoria.port.out;

import com.psnf.api.psnf.domain.categoria.model.Categoria;

import java.util.List;
import java.util.UUID;

public interface CategoriaRepositoryPort {

    Categoria save(Categoria categoria);

    List<Categoria> buscarTodas();

    boolean existePorId(UUID categoriaId);

    boolean existePorNome(String nome);
}
