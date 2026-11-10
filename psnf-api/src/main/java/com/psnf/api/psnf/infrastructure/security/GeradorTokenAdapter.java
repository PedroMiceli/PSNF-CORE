package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.autenticacao.port.out.GeradorTokenPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Tokens opacos (refresh, confirmação de e-mail, recuperação de senha). SHA-256 basta para guardá-los:
 * diferente de senha, eles têm 256 bits aleatórios, não dá para adivinhar por força bruta.
 */
@Component
public class GeradorTokenAdapter implements GeradorTokenPort {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String gerar() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
