package com.psnf.api.psnf.application.anuncio.event;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioElasticMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.elastic.AnuncioElasticDocument;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioElasticRepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.ImagemAnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.ValorAnuncioJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.categoria.repository.CategoriaJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AnuncioIndexacaoListener {
    private static final Logger logger = LoggerFactory.getLogger(AnuncioIndexacaoListener.class);

    @Autowired
    private AnuncioJPARepository anuncioJPARepository;

    @Autowired
    private ImagemAnuncioJPARepository imagemAnuncioJPARepository;

    @Autowired
    private ValorAnuncioJpaRepository valorAnuncioJpaRepository;

    @Autowired
    private AnuncioElasticRepository anuncioElasticRepository;

    @Autowired
    private CategoriaJpaRepository categoriaJpaRepository;

    @Autowired
    private UsuarioJPARepository usuarioJPARepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void indexar(AnuncioSalvoEvent event) {
        UUID anuncioId = event.anuncioId();
        try {
            AnuncioJpa anuncio = anuncioJPARepository.findById(anuncioId)
                    .orElseThrow(() -> new IllegalStateException("Anúncio não encontrado: " + anuncioId));

            String imagem = imagemAnuncioJPARepository.buscarCaminhoImagemPrincipalPorAnuncioId(anuncioId)
                    .orElseThrow(() -> new IllegalStateException("Imagem principal não encontrada para o anúncio: " + anuncioId));

            BigDecimal valor = valorAnuncioJpaRepository.buscarMenorValorUnidade(anuncioId)
                    .orElseThrow(() -> new IllegalStateException("Nenhum valor cadastrado para o anúncio: " + anuncioId));

            String categoriaNome = categoriaJpaRepository.buscarNomePorId(anuncio.getCategoria())
                    .orElseThrow(() -> new IllegalStateException("Categoria não encontrada: " + anuncio.getCategoria()));

            String nomeVendedor = usuarioJPARepository.buscarNomePorId(anuncio.getVendedor())
                    .orElseThrow(() -> new IllegalStateException("Vendedor não encontrado: " + anuncio.getVendedor()));

            AnuncioElasticDocument documento = AnuncioElasticMapper.toElasticDocument(
                    AnuncioJpaMapper.toDomain(anuncio),
                    imagem,
                    categoriaNome,
                    valor,
                    nomeVendedor
            );

            anuncioElasticRepository.save(documento);
        } catch (Exception e) {
            logger.error("Falha ao indexar anúncio {} no Elasticsearch", event.anuncioId(), e);
            //não deixa isso vazar pro chamador
        }
    }
}
