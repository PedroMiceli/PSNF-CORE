package com.psnf.api.psnf.domain.negociacao.port.in;

import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;

import java.util.List;
import java.util.UUID;

public interface ConsultarNegociacaoUseCase {

    NegociacaoDetalhes buscarPorId(UUID negociacaoId, UUID usuarioId);

    List<NegociacaoDetalhes> listarDoUsuario(UUID usuarioId);
}
