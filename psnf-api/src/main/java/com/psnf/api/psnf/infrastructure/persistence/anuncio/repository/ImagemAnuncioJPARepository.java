package com.psnf.api.psnf.infrastructure.persistence.anuncio.repository;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ImagemAnuncioJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImagemAnuncioJPARepository extends JpaRepository<ImagemAnuncioJpa, UUID> {

    @Query("SELECT c.caminho FROM ImagemAnuncioJpa c WHERE c.id = :id")
    Optional<String> buscarCaminhoImagemPrincipalPorAnuncioId(@Param("anuncioId") UUID id);
}
