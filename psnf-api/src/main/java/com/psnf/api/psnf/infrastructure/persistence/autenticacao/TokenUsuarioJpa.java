package com.psnf.api.psnf.infrastructure.persistence.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.model.TipoTokenUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tabela criada na migração V3. */
@Entity
@Table(name = "token_usuario")
@NoArgsConstructor
@Getter
@Setter
public class TokenUsuarioJpa {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID usuarioId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTokenUsuario tipo;
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(nullable = false)
    private LocalDateTime criadoEm;
    @Column(nullable = false)
    private LocalDateTime expiraEm;
    private LocalDateTime usadoEm;
}
