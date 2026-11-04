package com.psnf.api.psnf.domain.categoria.port.in;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.domain.categoria.port.in.command.CadastrarCategoriaCommand;

public interface CadastrarCategoriaUseCase {

    Categoria cadastrarCategoria(CadastrarCategoriaCommand command);
}
