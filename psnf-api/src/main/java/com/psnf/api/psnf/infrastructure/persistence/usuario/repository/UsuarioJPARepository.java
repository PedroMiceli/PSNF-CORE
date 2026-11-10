package com.psnf.api.psnf.infrastructure.persistence.usuario.repository;

import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioJPARepository extends JpaRepository<UsuarioJpa, UUID> {

    @Query("SELECT u.nome FROM UsuarioJpa u WHERE u.id = :id")
    Optional<String> buscarNomePorId(@Param("id") UUID id);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UsuarioJpa> findByEmailIgnoreCase(String email);
}
