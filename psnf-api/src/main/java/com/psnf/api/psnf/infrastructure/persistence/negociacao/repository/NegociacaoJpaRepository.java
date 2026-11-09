package com.psnf.api.psnf.infrastructure.persistence.negociacao.repository;

import com.psnf.api.psnf.infrastructure.persistence.negociacao.entity.MensagemNegociacaoJpa;
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

    @Query("select count(n) > 0 from NegociacaoJpa n where n.id = :id and (n.comprador.id = :u or n.vendedor.id = :u)")
    boolean participa(@Param("id") UUID negociacaoId, @Param("u") UUID usuarioId);

    /** usa o índice (negociacao_id, data_cadastro) */
    @Query("""
            select m from MensagemNegociacaoJpa m
            where m.negociacao.id = :id
              and m.dataCadastro > (select d.dataCadastro from MensagemNegociacaoJpa d where d.id = :depois)
            order by m.dataCadastro
            """)
    List<MensagemNegociacaoJpa> mensagensDepois(@Param("id") UUID negociacaoId, @Param("depois") UUID mensagemId);
}
