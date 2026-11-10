package com.psnf.api.psnf.domain.autenticacao.port.out;

import com.psnf.api.psnf.domain.autenticacao.model.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

    RefreshToken salvar(RefreshToken token);

    Optional<RefreshToken> buscarPorHash(String tokenHash);

    /**
     * Marca como usado só se ainda não foi (UPDATE ... WHERE usado_em IS NULL).
     * @return false se outra requisição usou o token antes (corrida entre abas ou reuso)
     */
    boolean marcarUsado(UUID id, LocalDateTime agora);

    void revogarFamilia(UUID familia, LocalDateTime agora);

    void revogarDoUsuario(UUID usuarioId, LocalDateTime agora);

    /** apaga sessões vencidas há mais de um dia */
    int apagarExpirados(LocalDateTime limite);
}
