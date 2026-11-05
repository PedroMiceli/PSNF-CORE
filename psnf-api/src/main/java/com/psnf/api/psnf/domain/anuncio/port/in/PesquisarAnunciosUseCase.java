package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.domain.shared.model.Pagina;

/**
 * Vitrine e pesquisa por texto dos anúncios ativos.
 * Sem termo: mais recentes primeiro. Com termo: mais relevantes primeiro.
 */
public interface PesquisarAnunciosUseCase {

    Pagina<Anuncio> pesquisar(PesquisarAnunciosQuery query);
}
