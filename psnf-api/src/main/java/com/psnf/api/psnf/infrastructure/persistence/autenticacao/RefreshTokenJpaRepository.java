package com.psnf.api.psnf.infrastructure.persistence.autenticacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpa, UUID> {

    Optional<RefreshTokenJpa> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshTokenJpa t set t.usadoEm = :agora where t.id = :id and t.usadoEm is null")
    int marcarUsado(@Param("id") UUID id, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshTokenJpa t set t.revogadoEm = :agora where t.familia = :familia and t.revogadoEm is null")
    int revogarFamilia(@Param("familia") UUID familia, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshTokenJpa t set t.revogadoEm = :agora where t.usuarioId = :usuarioId and t.revogadoEm is null")
    int revogarDoUsuario(@Param("usuarioId") UUID usuarioId, @Param("agora") LocalDateTime agora);

    @Modifying
    @Query("delete from RefreshTokenJpa t where t.expiraEm < :limite")
    int apagarExpirados(@Param("limite") LocalDateTime limite);
}
