package com.psnf.api.psnf.infrastructure.persistence.auditoria;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventoAuditoriaJpaRepository extends JpaRepository<EventoAuditoriaJpa, UUID> {

    List<EventoAuditoriaJpa> findByUsuarioIdOrderByCriadoEmDesc(UUID usuarioId, Pageable pagina);

    @Modifying
    @Query("delete from EventoAuditoriaJpa e where e.criadoEm < :limite")
    int apagarAnterioresA(@Param("limite") LocalDateTime limite);
}
