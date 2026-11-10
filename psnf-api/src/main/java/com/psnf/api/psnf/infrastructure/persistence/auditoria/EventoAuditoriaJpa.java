package com.psnf.api.psnf.infrastructure.persistence.auditoria;

import com.psnf.api.psnf.domain.auditoria.model.TipoEventoAuditoria;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tabela criada na migração V4. */
@Entity
@Table(name = "evento_auditoria")
@NoArgsConstructor
@Getter
@Setter
public class EventoAuditoriaJpa {
    @Id
    private UUID id;
    private UUID usuarioId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEventoAuditoria tipo;
    @Column(length = 500)
    private String detalhe;
    @Column(length = 64)
    private String ip;
    @Column(length = 255)
    private String userAgent;
    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
