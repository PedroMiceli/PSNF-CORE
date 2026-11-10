package com.psnf.api.psnf.domain.autenticacao.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma sessão de login renovável. O valor do token nunca é guardado, só o hash.
 *
 * @param familia o mesmo login ao longo das rotações: revogar a família encerra a sessão inteira
 */
public record RefreshToken(UUID id, UUID usuarioId, UUID familia, String tokenHash, LocalDateTime criadoEm,
                           LocalDateTime expiraEm, LocalDateTime usadoEm, LocalDateTime revogadoEm,
                           String ip, String userAgent) {

    public static RefreshToken novo(UUID usuarioId, UUID familia, String tokenHash, Duration validade,
                                    String ip, String userAgent, LocalDateTime agora) {
        return new RefreshToken(UUID.randomUUID(), usuarioId, familia, tokenHash, agora, agora.plus(validade),
                null, null, ip, userAgent);
    }

    public boolean expirado(LocalDateTime agora) { return !agora.isBefore(expiraEm); }

    public boolean revogado() { return revogadoEm != null; }

    public boolean usado() { return usadoEm != null; }

    /**
     * Usado há pouco: típico de duas abas renovando ao mesmo tempo com o mesmo cookie. Não é roubo;
     * a segunda aba recebe só um access token novo (o cookie de refresh já foi trocado pela primeira).
     */
    public boolean usadoHaMenosDe(Duration janela, LocalDateTime agora) {
        return usadoEm != null && agora.isBefore(usadoEm.plus(janela));
    }
}
