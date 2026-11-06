package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Carrega os anúncios e os participantes de uma negociação. */
@Component
@RequiredArgsConstructor
class DetalhesNegociacaoLoader {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    NegociacaoDetalhes carregar(Negociacao n) {
        List<Anuncio> anuncios = n.getAnuncioIds().stream().map(anuncioRepositoryPort::buscarPorId).toList();
        return new NegociacaoDetalhes(n, anuncios.getFirst(), anuncios,
                usuarioRepositoryPort.buscarPorId(n.getCompradorId()),
                usuarioRepositoryPort.buscarPorId(n.getVendedorId()));
    }
}
