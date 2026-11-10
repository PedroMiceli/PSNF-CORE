package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.autenticacao.port.out.RefreshTokenRepositoryPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.TokenUsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Apaga sessões e links de e-mail vencidos (uma vez por hora). Com várias instâncias, todas rodam: não tem problema,
 * o DELETE é idempotente e barato (índice em expira_em).
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
public class LimpezaSessoesJob {

    private static final Logger logger = LoggerFactory.getLogger(LimpezaSessoesJob.class);
    private final RefreshTokenRepositoryPort refreshTokens;
    private final TokenUsuarioRepositoryPort tokensUsuario;

    @Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT1H")
    @Transactional
    public void limpar() {
        LocalDateTime limite = LocalDateTime.now().minusDays(1);
        int sessoes = refreshTokens.apagarExpirados(limite);
        int links = tokensUsuario.apagarExpirados(limite);
        if (sessoes + links > 0) logger.info("Limpeza: {} sessão(ões) e {} link(s) de e-mail vencidos apagados", sessoes, links);
    }
}
