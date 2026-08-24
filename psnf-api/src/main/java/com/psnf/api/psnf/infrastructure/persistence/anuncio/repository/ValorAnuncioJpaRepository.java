package com.psnf.api.psnf.infrastructure.persistence.anuncio.repository;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ValorJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValorAnuncioJpaRepository extends JpaRepository<ValorJpa, UUID> {

    @Query("SELECT MIN(v.valorUnidade) FROM ValorJpa v WHERE v.anuncio.id = :anuncioId")
    Optional<BigDecimal> buscarMenorValorUnidade(@Param("anuncioId") UUID anuncioId);
}
