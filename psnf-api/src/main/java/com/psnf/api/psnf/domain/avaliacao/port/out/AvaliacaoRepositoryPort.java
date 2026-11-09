package com.psnf.api.psnf.domain.avaliacao.port.out;

import com.psnf.api.psnf.domain.avaliacao.model.Avaliacao;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoRepositoryPort {

    Avaliacao save(Avaliacao avaliacao);

    void remover(UUID avaliacaoId);

    Optional<Avaliacao> buscarPorAnuncioEAutor(UUID anuncioId, UUID autorId);

    /** Avaliações do anúncio, das mais recentes para as mais antigas. */
    List<Avaliacao> listarPorAnuncio(UUID anuncioId);

    /** Nota média e quantidade de vários anúncios de uma vez (vitrine). Anúncios sem avaliação ficam de fora. */
    Map<UUID, ResumoAvaliacoes> resumoPorAnuncios(Collection<UUID> anuncioIds);
}
