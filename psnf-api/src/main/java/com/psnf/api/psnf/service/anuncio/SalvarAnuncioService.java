package com.psnf.api.psnf.service.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.SalvarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalvarAnuncioService implements SalvarAnuncioUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;

    private ApplicationEventPublisher eventPublisher;

    @Override
    public Anuncio salvarAnuncio(SalvarAnuncioCommand anuncio) {
        Anuncio anuncioDomain = Anuncio.criar(anuncio);

        return anuncioRepositoryPort.save(anuncio);
    }
}
