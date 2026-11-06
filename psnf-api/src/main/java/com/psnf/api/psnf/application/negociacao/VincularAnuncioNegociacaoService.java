package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.negociacao.port.in.VincularAnuncioNegociacaoUseCase;
import com.psnf.api.psnf.domain.negociacao.port.out.NegociacaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VincularAnuncioNegociacaoService implements VincularAnuncioNegociacaoUseCase {

    private final NegociacaoRepositoryPort negociacaoRepositoryPort;
    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final DetalhesNegociacaoLoader loader;

    @Override
    @Transactional
    public NegociacaoDetalhes adicionar(UUID negociacaoId, UUID usuarioId, UUID anuncioId) {
        Negociacao n = negociacaoRepositoryPort.buscarPorId(negociacaoId);
        n.garantirParticipante(usuarioId);
        n.adicionarAnuncio(usuarioId, anuncioRepositoryPort.buscarPorId(anuncioId));
        return loader.carregar(negociacaoRepositoryPort.save(n));
    }

    @Override
    @Transactional
    public NegociacaoDetalhes remover(UUID negociacaoId, UUID usuarioId, UUID anuncioId) {
        Negociacao n = negociacaoRepositoryPort.buscarPorId(negociacaoId);
        n.garantirParticipante(usuarioId);
        n.removerAnuncio(usuarioId, anuncioRepositoryPort.buscarPorId(anuncioId));
        return loader.carregar(negociacaoRepositoryPort.save(n));
    }
}
