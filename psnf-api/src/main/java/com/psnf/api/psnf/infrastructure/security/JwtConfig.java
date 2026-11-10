package com.psnf.api.psnf.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * JWT assinado com HMAC-SHA256 (chave simétrica): quem emite é quem valida, a própria API.
 * Se um dia outro serviço precisar validar os tokens sem poder emiti-los, troca-se por RSA/EC.
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class JwtConfig {

    static final String EMISSOR = "psnf";
    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    SecretKey chaveJwt(AuthProperties props) {
        byte[] bytes;
        if (props.jwtSegredo() == null || props.jwtSegredo().isBlank()) {
            logger.warn("psnf.auth.jwt-segredo (PSNF_JWT_SECRET) não definido: usando uma chave aleatória. "
                    + "Todo reinício encerra as sessões e cada instância terá uma chave diferente. NÃO use em produção.");
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = props.jwtSegredo().getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("psnf.auth.jwt-segredo precisa ter pelo menos 32 caracteres (256 bits)");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chave) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        // confere assinatura, validade (exp/nbf, com 60 s de tolerância de relógio) e o emissor
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISSOR));
        return decoder;
    }
}
