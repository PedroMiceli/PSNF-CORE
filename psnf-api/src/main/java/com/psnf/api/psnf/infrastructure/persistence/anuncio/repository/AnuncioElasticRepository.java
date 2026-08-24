package com.psnf.api.psnf.infrastructure.persistence.anuncio.repository;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.elastic.AnuncioElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AnuncioElasticRepository extends ElasticsearchRepository<AnuncioElasticDocument, String> {

    List<AnuncioElasticDocument> findByAtivoTrueAndTituloContainingIgnoreCase(String titulo);

    List<AnuncioElasticDocument> findByCategoriaIdAndAtivoTrue(String categoriaId);
}
