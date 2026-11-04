package com.psnf.api.psnf.application.categoria;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.domain.categoria.port.in.CadastrarCategoriaUseCase;
import com.psnf.api.psnf.domain.categoria.port.in.command.CadastrarCategoriaCommand;
import com.psnf.api.psnf.domain.categoria.port.out.CategoriaRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastrarCategoriaService implements CadastrarCategoriaUseCase {

    private final CategoriaRepositoryPort categoriaRepositoryPort;

    @Override
    @Transactional
    public Categoria cadastrarCategoria(CadastrarCategoriaCommand command) {
        if (categoriaRepositoryPort.existePorNome(command.nome().trim())) {
            throw new RegraDeNegocioException("Já existe uma categoria com o nome: " + command.nome());
        }
        return categoriaRepositoryPort.save(Categoria.criar(command.nome()));
    }
}
