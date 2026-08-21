package com.psnf.api.psnf.anuncio.event;

import com.psnf.api.psnf.anuncio.document.AnuncioDocument;
import com.psnf.api.psnf.anuncio.mapper.AnuncioDocumentMapper;
import com.psnf.api.psnf.anuncio.adapters.outbound.repositories.AnuncioElasticRepository;
import com.psnf.api.psnf.anuncio.adapters.outbound.repositories.AnuncioJPARepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AnuncioIndexacaoListener {
    private static final Logger logger = LoggerFactory.getLogger(AnuncioIndexacaoListener.class);


    @Autowired
    private AnuncioJPARepository anuncioJPARepository;

    @Autowired
    private AnuncioElasticRepository anuncioElasticRepository;

    @Autowired
    private AnuncioDocumentMapper anuncioDocumentMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void indexar(AnuncioSalvoEvent event) {
        try {
            anuncioJPARepository.findById(event.anuncioId()).ifPresent(anuncio -> {
                AnuncioDocument documento = anuncioDocumentMapper.toDocument(anuncio);
                anuncioElasticRepository.save(documento);
            });
        } catch (Exception e) {
            logger.error("Falha ao indexar anúncio {} no Elasticsearch", event.anuncioId(), e);
            //não deixa isso vazar pro chamador
        }
    }
}
