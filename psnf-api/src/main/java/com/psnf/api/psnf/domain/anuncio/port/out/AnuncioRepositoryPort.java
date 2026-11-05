package com.psnf.api.psnf.domain.anuncio.port.out;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.domain.shared.model.Pagina;

import java.util.List;
import java.util.UUID;

public interface AnuncioRepositoryPort {

    Anuncio save(Anuncio anuncio);

    Anuncio buscarPorId(UUID anuncioId);

    List<Anuncio> buscarPorVendedor(UUID vendedorId);

    /** Pesquisa paginada nos anúncios ativos (busca de texto feita no próprio PostgreSQL). */
    Pagina<Anuncio> pesquisar(PesquisarAnunciosQuery query);
}
