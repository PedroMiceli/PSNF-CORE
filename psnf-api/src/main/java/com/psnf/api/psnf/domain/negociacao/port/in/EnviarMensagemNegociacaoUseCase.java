package com.psnf.api.psnf.domain.negociacao.port.in;

import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;

import java.util.UUID;

public interface EnviarMensagemNegociacaoUseCase {

    NegociacaoDetalhes enviar(UUID negociacaoId, UUID autorId, String texto);
}
