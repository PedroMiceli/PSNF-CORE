package com.psnf.api.psnf.domain.anuncio.port.out;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.AnuncioResumo;

import java.util.List;
import java.util.UUID;

/**
 * Porta para o índice de busca de anúncios (implementada com Elasticsearch).
 */
public interface AnuncioIndexPort {

    void indexar(Anuncio anuncio);

    List<AnuncioResumo> pesquisar(String termo, UUID categoriaId);
}
