package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.SalvarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalvarAnuncioService implements SalvarAnuncioUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private ApplicationEventPublisher eventPublisher;


    @Override
    public Anuncio salvarAnuncio(CadastrarAnuncioCommand anuncio) {
        UUID idUsuarioLogado = UUID.fromString("1b713366-3be1-4485-b7b3-f2c61b70224a");

        Anuncio anuncioDomain = Anuncio.criar(anuncio.titulo(),anuncio.descricao(),true,anuncio.categoriaId(),idUsuarioLogado);
        return anuncioRepositoryPort.save(anuncioDomain);
    }

}
