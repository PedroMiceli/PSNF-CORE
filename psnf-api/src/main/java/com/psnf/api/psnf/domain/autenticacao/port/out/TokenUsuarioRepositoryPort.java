package com.psnf.api.psnf.domain.autenticacao.port.out;

import com.psnf.api.psnf.domain.autenticacao.model.TipoTokenUsuario;
import com.psnf.api.psnf.domain.autenticacao.model.TokenUsuario;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TokenUsuarioRepositoryPort {

    TokenUsuario salvar(TokenUsuario token);

    Optional<TokenUsuario> buscarPorHash(String tokenHash, TipoTokenUsuario tipo);

    /** marca como usado só se ainda não foi; false = outra requisição usou antes */
    boolean marcarUsado(UUID id, LocalDateTime agora);

    /** pedir um link novo invalida os anteriores do mesmo tipo */
    void invalidarDoUsuario(UUID usuarioId, TipoTokenUsuario tipo, LocalDateTime agora);

    /** quando foi gerado o último link deste tipo (para não encher a caixa de entrada) */
    Optional<LocalDateTime> ultimoCriadoEm(UUID usuarioId, TipoTokenUsuario tipo);

    int apagarExpirados(LocalDateTime limite);
}
