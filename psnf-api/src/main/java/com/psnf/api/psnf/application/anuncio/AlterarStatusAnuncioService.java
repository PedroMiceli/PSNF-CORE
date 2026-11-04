package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.application.anuncio.event.AnuncioSalvoEvent;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.AlterarStatusAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AlterarStatusAnuncioService implements AlterarStatusAnuncioUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Anuncio ativar(UUID anuncioId, UUID usuarioId) {
        return alterar(anuncioId, usuarioId, Anuncio::ativar);
    }

    @Override
    @Transactional
    public Anuncio desativar(UUID anuncioId, UUID usuarioId) {
        return alterar(anuncioId, usuarioId, Anuncio::desativar);
    }

    private Anuncio alterar(UUID anuncioId, UUID usuarioId, Consumer<Anuncio> alteracao) {
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(anuncioId);
        if (!anuncio.pertenceA(usuarioId)) {
            throw new AcessoNegadoException("Apenas o vendedor pode alterar o anúncio");
        }
        alteracao.accept(anuncio);
        Anuncio salvo = anuncioRepositoryPort.save(anuncio);
        eventPublisher.publishEvent(new AnuncioSalvoEvent(salvo.getId()));
        return salvo;
    }
}
