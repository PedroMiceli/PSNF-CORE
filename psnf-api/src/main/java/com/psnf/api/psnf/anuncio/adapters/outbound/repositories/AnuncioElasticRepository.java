package com.psnf.api.psnf.anuncio.adapters.outbound.repositories;

import com.psnf.api.psnf.anuncio.document.AnuncioDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AnuncioElasticRepository extends ElasticsearchRepository<AnuncioDocument, String> {

    List<AnuncioDocument> findByAtivoTrueAndTituloContainingIgnoreCase(String titulo);

    List<AnuncioDocument> findByCategoriaIdAndAtivoTrue(String categoriaId);
}
