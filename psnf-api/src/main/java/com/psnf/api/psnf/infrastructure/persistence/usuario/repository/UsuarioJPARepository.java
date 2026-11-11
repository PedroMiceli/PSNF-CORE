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

    @Query("select count(u) > 0 from UsuarioJpa u where u.cnpj = :cnpj and (:exceto is null or u.id <> :exceto)")
    boolean cnpjEmUso(@Param("cnpj") String cnpj, @Param("exceto") UUID exceto);

    Optional<UsuarioJpa> findByEmailIgnoreCase(String email);
}
