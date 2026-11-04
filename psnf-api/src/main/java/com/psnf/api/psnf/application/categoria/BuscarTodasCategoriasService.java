package com.psnf.api.psnf.application.categoria;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.domain.categoria.port.in.BuscarTodasCategoriasUseCase;
import com.psnf.api.psnf.domain.categoria.port.out.CategoriaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodasCategoriasService implements BuscarTodasCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> buscarTodasCategorias() {
        return categoriaRepositoryPort.buscarTodas();
    }
}
