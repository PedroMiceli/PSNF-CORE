package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.auditoria.port.out.AuditoriaPort;
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
 * Apaga sessões e links de e-mail vencidos e auditoria com mais de 1 ano (uma vez por hora). Com várias instâncias, todas rodam: não tem problema,
 * o DELETE é idempotente e barato (índice em expira_em).
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
public class LimpezaSessoesJob {

    private static final Logger logger = LoggerFactory.getLogger(LimpezaSessoesJob.class);
    private final RefreshTokenRepositoryPort refreshTokens;
    private final TokenUsuarioRepositoryPort tokensUsuario;
    private final AuditoriaPort auditoria;

    @Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT1H")
    @Transactional
    public void limpar() {
        LocalDateTime limite = LocalDateTime.now().minusDays(1);
        int sessoes = refreshTokens.apagarExpirados(limite);
        int links = tokensUsuario.apagarExpirados(limite);
        int eventos = auditoria.apagarAnterioresA(LocalDateTime.now().minusYears(1)); // auditoria guarda 1 ano
        if (sessoes + links + eventos > 0) {
            logger.info("Limpeza: {} sessão(ões), {} link(s) de e-mail e {} evento(s) de auditoria antigos apagados",
                    sessoes, links, eventos);
        }
    }
}
