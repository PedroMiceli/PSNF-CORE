package com.psnf.api.psnf.infrastructure.persistence.usuario.repository;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJPARepository extends JpaRepository<Usuario, UUID> {

    @Query("SELECT c.nome FROM UsuarioJpa c WHERE c.id = :id")
    Optional<String> buscarNomePorId(@Param("id") UUID id);

}
