package com.psnf.api.psnf.domain.avaliacao.port.in;

import com.psnf.api.psnf.domain.avaliacao.model.AvaliacoesDoAnuncio;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AvaliarAnuncioUseCase {

    /** Avaliações do anúncio e o que o usuário logado pode fazer (usuarioId pode ser nulo). */
    AvaliacoesDoAnuncio listar(UUID anuncioId, UUID usuarioId);

    /** O comprador publica (ou edita) a sua avaliação do produto. */
    AvaliacoesDoAnuncio avaliar(UUID anuncioId, UUID usuarioId, int nota, String texto, List<String> imagens);

    /** O comprador exclui a sua avaliação. */
    AvaliacoesDoAnuncio remover(UUID anuncioId, UUID usuarioId);

    Map<UUID, ResumoAvaliacoes> resumoPorAnuncios(Collection<UUID> anuncioIds);
}
