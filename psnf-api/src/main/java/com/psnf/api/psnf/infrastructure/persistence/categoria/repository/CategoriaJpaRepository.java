package com.psnf.api.psnf.infrastructure.persistence.categoria.repository;

import com.psnf.api.psnf.infrastructure.persistence.categoria.entity.CategoriaJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpa, UUID> {

    @Query("SELECT c.nome FROM CategoriaJpa c WHERE c.id = :id")
    Optional<String> buscarNomePorId(@Param("id") UUID id);

    boolean existsByNomeIgnoreCase(String nome);

    List<CategoriaJpa> findAllByOrderByNomeAsc();
}
