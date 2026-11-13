package com.psnf.api.psnf.infrastructure.web.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Escolhe onde ficam os baldes do rate limit e do freio de tentativas de login
 * (psnf.rate-limit.armazenamento): "redis" (padrão, global) ou "memoria" (por instância, sem Redis).
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    @Bean
    public ArmazenamentoBaldes armazenamentoBaldes(RateLimitProperties props, ObjectProvider<StringRedisTemplate> redis) {
        BaldesEmMemoria memoria = new BaldesEmMemoria(props.maxClientes());
        if (!props.usaRedis()) {
            logger.info("Rate limit: baldes em memória (por instância)");
            return memoria;
        }
        logger.info("Rate limit: baldes no Redis (limite global entre as instâncias)");
        return new BaldesRedis(redis.getObject(), memoria);
    }
}
