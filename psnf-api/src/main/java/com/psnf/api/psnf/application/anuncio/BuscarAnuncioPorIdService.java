package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.BuscarAnuncioPorIdUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarAnuncioPorIdService implements BuscarAnuncioPorIdUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;

    @Override
    public Anuncio buscarPorId(UUID anuncio) {
        return anuncioRepositoryPort.buscarPorId(anuncio);
    }
}
