package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.AnuncioResumo;
import com.psnf.api.psnf.domain.anuncio.port.in.PesquisarAnunciosUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioIndexPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PesquisarAnunciosService implements PesquisarAnunciosUseCase {

    private final AnuncioIndexPort anuncioIndexPort;

    @Override
    public List<AnuncioResumo> pesquisar(String termo, UUID categoriaId) {
        return anuncioIndexPort.pesquisar(termo, categoriaId);
    }
}
