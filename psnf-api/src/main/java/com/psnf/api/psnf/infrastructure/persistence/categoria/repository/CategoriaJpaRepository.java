package com.psnf.api.psnf.infrastructure.persistence.categoria.repository;

import com.psnf.api.psnf.infrastructure.persistence.categoria.entity.CategoriaJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpa, UUID> {

    @Query("SELECT c.nome FROM CategoriaJpa c WHERE c.id = :id")
    Optional<String> buscarNomePorId(@Param("id") UUID id);

}
