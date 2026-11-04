package com.psnf.api.psnf.application.anuncio.event;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioIndexPort;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Após o commit do anúncio no banco, (re)indexa no índice de busca.
 * Falhas na indexação são apenas logadas: não podem desfazer o cadastro do anúncio.
 */
@Component
@RequiredArgsConstructor
public class AnuncioIndexacaoListener {

    private static final Logger logger = LoggerFactory.getLogger(AnuncioIndexacaoListener.class);

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final AnuncioIndexPort anuncioIndexPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void indexar(AnuncioSalvoEvent event) {
        try {
            Anuncio anuncio = anuncioRepositoryPort.buscarPorId(event.anuncioId());
            anuncioIndexPort.indexar(anuncio);
        } catch (Exception e) {
            logger.error("Falha ao indexar anúncio {} no Elasticsearch", event.anuncioId(), e);
        }
    }
}
