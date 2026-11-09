package com.psnf.api.psnf.domain.negociacao.port.in;

import com.psnf.api.psnf.domain.negociacao.model.MensagemNegociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;

import java.util.List;
import java.util.UUID;

public interface ConsultarNegociacaoUseCase {

    NegociacaoDetalhes buscarPorId(UUID negociacaoId, UUID usuarioId);

    List<NegociacaoDetalhes> listarDoUsuario(UUID usuarioId);

    /** Só as mensagens novas depois da última que a tela já tem (o chat aberto pergunta isso periodicamente). */
    List<MensagemNegociacao> mensagensDepois(UUID negociacaoId, UUID usuarioId, UUID ultimaMensagemId);
}
