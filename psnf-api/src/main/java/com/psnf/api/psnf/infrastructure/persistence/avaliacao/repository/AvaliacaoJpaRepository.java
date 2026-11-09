package com.psnf.api.psnf.infrastructure.persistence.avaliacao.repository;

import com.psnf.api.psnf.infrastructure.persistence.avaliacao.entity.AvaliacaoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoJpaRepository extends JpaRepository<AvaliacaoJpa, UUID> {

    Optional<AvaliacaoJpa> findByAnuncioIdAndAutorId(UUID anuncioId, UUID autorId);

    List<AvaliacaoJpa> findByAnuncioIdOrderByDataCadastroDesc(UUID anuncioId);

    /** [anuncioId, soma das notas, quantidade] por anúncio */
    @Query("select a.anuncio.id, sum(a.nota), count(a) from AvaliacaoJpa a where a.anuncio.id in :ids group by a.anuncio.id")
    List<Object[]> resumoPorAnuncios(@Param("ids") Collection<UUID> ids);
}
