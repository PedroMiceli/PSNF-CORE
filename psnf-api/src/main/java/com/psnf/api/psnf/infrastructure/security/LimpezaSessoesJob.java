package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.autenticacao.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Apaga sessões vencidas (uma vez por hora). Com várias instâncias, todas rodam: não tem problema,
 * o DELETE é idempotente e barato (índice em expira_em).
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
public class LimpezaSessoesJob {

    private static final Logger logger = LoggerFactory.getLogger(LimpezaSessoesJob.class);
    private final RefreshTokenRepositoryPort refreshTokens;

    @Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT1H")
    @Transactional
    public void limpar() {
        int apagados = refreshTokens.apagarExpirados(LocalDateTime.now().minusDays(1));
        if (apagados > 0) logger.info("Sessões vencidas apagadas: {}", apagados);
    }
}
