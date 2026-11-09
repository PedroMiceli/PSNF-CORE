package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.negociacao.model.MensagemNegociacao;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.negociacao.port.in.ConsultarNegociacaoUseCase;
import com.psnf.api.psnf.domain.negociacao.port.out.NegociacaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarNegociacaoService implements ConsultarNegociacaoUseCase {

    private final NegociacaoRepositoryPort negociacaoRepositoryPort;
    private final DetalhesNegociacaoLoader loader;

    @Override
    @Transactional(readOnly = true)
    public NegociacaoDetalhes buscarPorId(UUID negociacaoId, UUID usuarioId) {
        Negociacao n = negociacaoRepositoryPort.buscarPorId(negociacaoId);
        n.garantirParticipante(usuarioId);
        return loader.carregar(n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensagemNegociacao> mensagensDepois(UUID negociacaoId, UUID usuarioId, UUID ultimaMensagemId) {
        // duas consultas pequenas: confere a permissão e busca o delta (quase sempre vazio)
        if (!negociacaoRepositoryPort.participa(negociacaoId, usuarioId)) {
            throw new AcessoNegadoException("Apenas o comprador e o vendedor participam desta negociação");
        }
        return negociacaoRepositoryPort.mensagensDepois(negociacaoId, ultimaMensagemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NegociacaoDetalhes> listarDoUsuario(UUID usuarioId) {
        return negociacaoRepositoryPort.listarPorParticipante(usuarioId).stream().map(loader::carregar).toList();
    }
}
