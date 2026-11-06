package com.psnf.api.psnf.domain.negociacao.port.in;

import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;

import java.util.UUID;

/** Inclui ou retira produtos do mesmo vendedor numa negociação já aberta. */
public interface VincularAnuncioNegociacaoUseCase {

    NegociacaoDetalhes adicionar(UUID negociacaoId, UUID usuarioId, UUID anuncioId);

    NegociacaoDetalhes remover(UUID negociacaoId, UUID usuarioId, UUID anuncioId);
}
