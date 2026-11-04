package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.AnuncioResumo;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioIndexPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioElasticMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.elastic.AnuncioElasticDocument;
import com.psnf.api.psnf.infrastructure.persistence.categoria.repository.CategoriaJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Usa {@link ElasticsearchOperations} diretamente (em vez de um ElasticsearchRepository) para que a aplicação
 * consiga subir mesmo com o Elasticsearch fora do ar — o índice é criado sob demanda na primeira indexação.
 */
@Component
@RequiredArgsConstructor
public class AnuncioElasticAdapter implements AnuncioIndexPort {

    private final ElasticsearchOperations elasticsearchOperations;
    private final CategoriaJpaRepository categoriaJpaRepository;
    private final UsuarioJPARepository usuarioJPARepository;

    private volatile boolean indiceVerificado = false;

    @Override
    public void indexar(Anuncio anuncio) {
        garantirIndice();

        String categoriaNome = categoriaJpaRepository.buscarNomePorId(anuncio.getCategoria())
                .orElseThrow(() -> new IllegalStateException("Categoria não encontrada: " + anuncio.getCategoria()));

        String vendedorNome = usuarioJPARepository.buscarNomePorId(anuncio.getVendedor())
                .orElseThrow(() -> new IllegalStateException("Vendedor não encontrado: " + anuncio.getVendedor()));

        elasticsearchOperations.save(AnuncioElasticMapper.toElasticDocument(anuncio, categoriaNome, vendedorNome));
    }

    @Override
    public List<AnuncioResumo> pesquisar(String termo, UUID categoriaId) {
        garantirIndice();

        Criteria criteria = new Criteria("ativo").is(true);

        if (termo != null && !termo.isBlank()) {
            criteria = criteria.subCriteria(
                    new Criteria("titulo").matches(termo).or("descricao").matches(termo));
        }
        if (categoriaId != null) {
            criteria = criteria.and(new Criteria("categoriaId").is(categoriaId.toString()));
        }

        return elasticsearchOperations.search(new CriteriaQuery(criteria), AnuncioElasticDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .map(AnuncioElasticMapper::toResumo)
                .toList();
    }

    private void garantirIndice() {
        if (indiceVerificado) return;
        IndexOperations indexOps = elasticsearchOperations.indexOps(AnuncioElasticDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
        }
        indiceVerificado = true;
    }
}
