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
    private final com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Pagina<Anuncio> pesquisar(PesquisarAnunciosQuery query) {
        boolean temCnpj = query.visitanteId() != null && usuarioRepositoryPort.buscarPorId(query.visitanteId()).temCnpj();
        return anuncioRepositoryPort.pesquisar(query.paraVisitante(query.visitanteId(), temCnpj));
    }
}
