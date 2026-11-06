package com.psnf.api.psnf.infrastructure.persistence.negociacao.repository;

import com.psnf.api.psnf.infrastructure.persistence.negociacao.entity.NegociacaoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NegociacaoJpaRepository extends JpaRepository<NegociacaoJpa, UUID> {

    Optional<NegociacaoJpa> findByAnuncioIdAndCompradorId(UUID anuncioId, UUID compradorId);

    @Query("""
            select n from NegociacaoJpa n
            where n.comprador.id = :usuarioId or n.vendedor.id = :usuarioId
            order by coalesce(n.ultimaMensagemEm, n.dataCadastro) desc
            """)
    List<NegociacaoJpa> listarPorParticipante(@Param("usuarioId") UUID usuarioId);
}
