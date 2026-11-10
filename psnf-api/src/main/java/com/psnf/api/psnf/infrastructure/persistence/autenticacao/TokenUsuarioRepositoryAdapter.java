package com.psnf.api.psnf.infrastructure.persistence.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.model.TipoTokenUsuario;
import com.psnf.api.psnf.domain.autenticacao.model.TokenUsuario;
import com.psnf.api.psnf.domain.autenticacao.port.out.TokenUsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenUsuarioRepositoryAdapter implements TokenUsuarioRepositoryPort {

    private final TokenUsuarioJpaRepository repo;

    @Override
    public TokenUsuario salvar(TokenUsuario t) {
        TokenUsuarioJpa e = new TokenUsuarioJpa();
        e.setId(t.id());
        e.setUsuarioId(t.usuarioId());
        e.setTipo(t.tipo());
        e.setTokenHash(t.tokenHash());
        e.setCriadoEm(t.criadoEm());
        e.setExpiraEm(t.expiraEm());
        e.setUsadoEm(t.usadoEm());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<TokenUsuario> buscarPorHash(String tokenHash, TipoTokenUsuario tipo) {
        return repo.findByTokenHashAndTipo(tokenHash, tipo).map(TokenUsuarioRepositoryAdapter::toDomain);
    }

    @Override
    public boolean marcarUsado(UUID id, LocalDateTime agora) { return repo.marcarUsado(id, agora) == 1; }

    @Override
    public void invalidarDoUsuario(UUID usuarioId, TipoTokenUsuario tipo, LocalDateTime agora) {
        repo.invalidar(usuarioId, tipo, agora);
    }

    @Override
    public Optional<LocalDateTime> ultimoCriadoEm(UUID usuarioId, TipoTokenUsuario tipo) {
        return repo.ultimoCriadoEm(usuarioId, tipo);
    }

    @Override
    public int apagarExpirados(LocalDateTime limite) { return repo.apagarExpirados(limite); }

    private static TokenUsuario toDomain(TokenUsuarioJpa e) {
        return new TokenUsuario(e.getId(), e.getUsuarioId(), e.getTipo(), e.getTokenHash(), e.getCriadoEm(),
                e.getExpiraEm(), e.getUsadoEm());
    }
}
