package com.psnf.api.psnf.domain.autenticacao.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** Link de uso único (confirmação de e-mail, redefinição de senha). Só o hash é guardado. */
public record TokenUsuario(UUID id, UUID usuarioId, TipoTokenUsuario tipo, String tokenHash,
                           LocalDateTime criadoEm, LocalDateTime expiraEm, LocalDateTime usadoEm) {

    public static TokenUsuario novo(UUID usuarioId, TipoTokenUsuario tipo, String tokenHash, LocalDateTime agora) {
        return new TokenUsuario(UUID.randomUUID(), usuarioId, tipo, tokenHash, agora, agora.plus(tipo.validade()), null);
    }

    public boolean valido(LocalDateTime agora) { return usadoEm == null && agora.isBefore(expiraEm); }
}
