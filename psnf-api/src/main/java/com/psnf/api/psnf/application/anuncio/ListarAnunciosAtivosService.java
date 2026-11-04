package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.ListarAnunciosAtivosUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListarAnunciosAtivosService implements ListarAnunciosAtivosUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Anuncio> listarAtivos(UUID categoriaId) {
        return anuncioRepositoryPort.buscarAtivos(categoriaId);
    }
}
