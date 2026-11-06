package com.psnf.api.psnf.domain.negociacao.port.in;

import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;

import java.util.UUID;

public interface IniciarNegociacaoUseCase {

    /** @param nova false quando o comprador já tinha uma negociação para este anúncio (ela é reaberta) */
    record Resultado(NegociacaoDetalhes detalhes, boolean nova) {
    }

    Resultado iniciar(UUID anuncioId, UUID compradorId);
}
