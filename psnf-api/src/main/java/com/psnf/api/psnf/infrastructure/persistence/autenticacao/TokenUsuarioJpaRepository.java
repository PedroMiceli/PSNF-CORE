package com.psnf.api.psnf.infrastructure.persistence.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.model.TipoTokenUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TokenUsuarioJpaRepository extends JpaRepository<TokenUsuarioJpa, UUID> {

    Optional<TokenUsuarioJpa> findByTokenHashAndTipo(String tokenHash, TipoTokenUsuario tipo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TokenUsuarioJpa t set t.usadoEm = :agora where t.id = :id and t.usadoEm is null")
    int marcarUsado(@Param("id") UUID id, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TokenUsuarioJpa t set t.usadoEm = :agora where t.usuarioId = :u and t.tipo = :tipo and t.usadoEm is null")
    int invalidar(@Param("u") UUID usuarioId, @Param("tipo") TipoTokenUsuario tipo, @Param("agora") LocalDateTime agora);

    @Query("select max(t.criadoEm) from TokenUsuarioJpa t where t.usuarioId = :u and t.tipo = :tipo")
    Optional<LocalDateTime> ultimoCriadoEm(@Param("u") UUID usuarioId, @Param("tipo") TipoTokenUsuario tipo);

    @Modifying
    @Query("delete from TokenUsuarioJpa t where t.expiraEm < :limite")
    int apagarExpirados(@Param("limite") LocalDateTime limite);
}
