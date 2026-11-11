package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.SalvarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.mapper.CadastrarAnuncioCommandMapper;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.categoria.exception.CategoriaNaoEncontradaException;
import com.psnf.api.psnf.domain.categoria.port.out.CategoriaRepositoryPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalvarAnuncioService implements SalvarAnuncioUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final CategoriaRepositoryPort categoriaRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional
    public Anuncio salvarAnuncio(CadastrarAnuncioCommand command) {
        // garante que o vendedor existe (lança 404 caso contrário)
        usuarioRepositoryPort.buscarPorId(command.vendedorId());

        if (!categoriaRepositoryPort.existePorId(command.categoriaId())) {
            throw new CategoriaNaoEncontradaException(command.categoriaId());
        }

        Anuncio anuncio = Anuncio.criar(
                command.titulo(),
                command.descricao(),
                command.categoriaId(),
                command.vendedorId(),
                CadastrarAnuncioCommandMapper.toValores(command.valores()),
                CadastrarAnuncioCommandMapper.toImagens(command.imagens()),
                CadastrarAnuncioCommandMapper.toVariacoes(command.variacoes())
        );

        anuncio.definirDetalhes(command.informacoes(), command.caracteristicas());
        anuncio.definirExigeCnpj(command.exigeCnpj());

        return anuncioRepositoryPort.save(anuncio);
    }
}
