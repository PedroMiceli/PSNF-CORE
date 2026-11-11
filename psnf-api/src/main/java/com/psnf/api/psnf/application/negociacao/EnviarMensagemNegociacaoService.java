package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.negociacao.port.in.EnviarMensagemNegociacaoUseCase;
import com.psnf.api.psnf.domain.negociacao.port.out.NegociacaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnviarMensagemNegociacaoService implements EnviarMensagemNegociacaoUseCase {

    private final NegociacaoRepositoryPort negociacaoRepositoryPort;
    private final DetalhesNegociacaoLoader loader;
    private final RegraCnpjNegociacao regraCnpj;

    @Override
    @Transactional
    public NegociacaoDetalhes enviar(UUID negociacaoId, UUID autorId, String texto) {
        Negociacao n = negociacaoRepositoryPort.buscarPorId(negociacaoId);
        n.garantirParticipante(autorId);
        regraCnpj.garantirAcesso(n, autorId);
        n.enviarMensagem(autorId, texto);
        return loader.carregar(negociacaoRepositoryPort.save(n));
    }
}
