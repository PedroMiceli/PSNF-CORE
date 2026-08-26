package com.psnf.api.psnf.domain.categoria.port.in;

import com.psnf.api.psnf.domain.categoria.model.Categoria;

import java.util.List;

public interface BuscarTodasCategoriasUseCase {

    List<Categoria>  buscarTodasCategorias();
}
