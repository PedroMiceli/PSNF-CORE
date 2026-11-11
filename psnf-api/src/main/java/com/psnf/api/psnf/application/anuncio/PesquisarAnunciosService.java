package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.PesquisarAnunciosUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.shared.model.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PesquisarAnunciosService implements PesquisarAnunciosUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Pagina<Anuncio> pesquisar(PesquisarAnunciosQuery query) {
        // todos veem todos os anúncios ativos; o CNPJ só conta na hora de negociar
        return anuncioRepositoryPort.pesquisar(query);
    }
}
