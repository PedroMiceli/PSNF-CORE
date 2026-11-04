package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.application.anuncio.event.AnuncioSalvoEvent;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.AtualizarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.command.AtualizarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.mapper.CadastrarAnuncioCommandMapper;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.categoria.exception.CategoriaNaoEncontradaException;
import com.psnf.api.psnf.domain.categoria.port.out.CategoriaRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarAnuncioService implements AtualizarAnuncioUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final CategoriaRepositoryPort categoriaRepositoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Anuncio atualizarAnuncio(AtualizarAnuncioCommand command) {
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(command.anuncioId());

        if (!anuncio.pertenceA(command.usuarioId())) {
            throw new AcessoNegadoException("Apenas o vendedor pode alterar o anúncio");
        }
        if (!categoriaRepositoryPort.existePorId(command.categoriaId())) {
            throw new CategoriaNaoEncontradaException(command.categoriaId());
        }

        anuncio.atualizar(
                command.titulo(),
                command.descricao(),
                command.categoriaId(),
                CadastrarAnuncioCommandMapper.toValores(command.valores()),
                CadastrarAnuncioCommandMapper.toImagens(command.imagens()),
                CadastrarAnuncioCommandMapper.toVariacoes(command.variacoes())
        );

        Anuncio anuncioSalvo = anuncioRepositoryPort.save(anuncio);
        eventPublisher.publishEvent(new AnuncioSalvoEvent(anuncioSalvo.getId()));
        return anuncioSalvo;
    }
}
