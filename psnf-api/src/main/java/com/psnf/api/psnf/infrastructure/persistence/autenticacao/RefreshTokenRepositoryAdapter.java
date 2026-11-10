package com.psnf.api.psnf.infrastructure.persistence.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.model.RefreshToken;
import com.psnf.api.psnf.domain.autenticacao.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repo;

    @Override
    public RefreshToken salvar(RefreshToken t) {
        RefreshTokenJpa e = new RefreshTokenJpa();
        e.setId(t.id());
        e.setUsuarioId(t.usuarioId());
        e.setFamilia(t.familia());
        e.setTokenHash(t.tokenHash());
        e.setCriadoEm(t.criadoEm());
        e.setExpiraEm(t.expiraEm());
        e.setUsadoEm(t.usadoEm());
        e.setRevogadoEm(t.revogadoEm());
        e.setIp(t.ip());
        e.setUserAgent(t.userAgent());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<RefreshToken> buscarPorHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(RefreshTokenRepositoryAdapter::toDomain);
    }

    @Override
    public boolean marcarUsado(UUID id, LocalDateTime agora) { return repo.marcarUsado(id, agora) == 1; }

    @Override
    public void revogarFamilia(UUID familia, LocalDateTime agora) { repo.revogarFamilia(familia, agora); }

    @Override
    public void revogarDoUsuario(UUID usuarioId, LocalDateTime agora) { repo.revogarDoUsuario(usuarioId, agora); }

    @Override
    public int apagarExpirados(LocalDateTime limite) { return repo.apagarExpirados(limite); }

    private static RefreshToken toDomain(RefreshTokenJpa e) {
        return new RefreshToken(e.getId(), e.getUsuarioId(), e.getFamilia(), e.getTokenHash(), e.getCriadoEm(),
                e.getExpiraEm(), e.getUsadoEm(), e.getRevogadoEm(), e.getIp(), e.getUserAgent());
    }
}
