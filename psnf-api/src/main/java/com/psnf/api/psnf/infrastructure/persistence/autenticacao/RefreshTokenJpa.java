package com.psnf.api.psnf.infrastructure.persistence.autenticacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tabela criada na migração V2__autenticacao.sql. */
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor
@Getter
@Setter
public class RefreshTokenJpa {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID usuarioId;
    @Column(nullable = false)
    private UUID familia;
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(nullable = false)
    private LocalDateTime criadoEm;
    @Column(nullable = false)
    private LocalDateTime expiraEm;
    private LocalDateTime usadoEm;
    private LocalDateTime revogadoEm;
    @Column(length = 64)
    private String ip;
    @Column(length = 255)
    private String userAgent;
}
