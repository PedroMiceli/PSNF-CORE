package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.BuscarAnuncioPorIdUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarAnuncioPorIdService implements BuscarAnuncioPorIdUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Anuncio buscarPorId(UUID anuncioId, UUID visitanteId) {
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(anuncioId);
        // anúncio só para empresas, visto por quem não tem CNPJ: responde como se não existisse
        if (!anuncio.visivelPara(visitanteId, temCnpj(anuncio, visitanteId))) {
            throw new com.psnf.api.psnf.domain.anuncio.exception.AnuncioNaoEncontradoException(anuncioId);
        }
        return anuncio;
    }

    private boolean temCnpj(Anuncio anuncio, UUID visitanteId) {
        return anuncio.isExigeCnpj() && visitanteId != null && !anuncio.pertenceA(visitanteId)
                && usuarioRepositoryPort.buscarPorId(visitanteId).temCnpj();
    }
}
