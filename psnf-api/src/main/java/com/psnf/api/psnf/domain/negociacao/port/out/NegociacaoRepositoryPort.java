package com.psnf.api.psnf.domain.negociacao.port.out;

import com.psnf.api.psnf.domain.negociacao.model.MensagemNegociacao;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NegociacaoRepositoryPort {

    Negociacao save(Negociacao negociacao);

    Negociacao buscarPorId(UUID id);

    Optional<Negociacao> buscarPorAnuncioEComprador(UUID anuncioId, UUID compradorId);

    /** O usuário é comprador ou vendedor desta negociação (consulta leve, sem carregar a conversa). */
    boolean participa(UUID negociacaoId, UUID usuarioId);

    /** Só as mensagens posteriores à mensagem informada, em ordem (chat por delta). */
    List<MensagemNegociacao> mensagensDepois(UUID negociacaoId, UUID mensagemId);

    /** Negociações em que o usuário é comprador ou vendedor, da conversa mais recente para a mais antiga. */
    List<Negociacao> listarPorParticipante(UUID usuarioId);
}
