package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.CategoriaComAnuncios;
import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.domain.shared.model.Pagina;

import java.util.List;
import java.util.UUID;

/**
 * Vitrine e pesquisa por texto dos anúncios ativos.
 * Sem termo: mais recentes primeiro. Com termo: mais relevantes primeiro.
 */
public interface PesquisarAnunciosUseCase {

    Pagina<Anuncio> pesquisar(PesquisarAnunciosQuery query);

    /** Página do vendedor: categorias em que ele tem anúncios ativos (para o filtro). */
    List<CategoriaComAnuncios> categoriasDoVendedor(UUID vendedorId);
}
