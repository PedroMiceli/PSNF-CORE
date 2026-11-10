package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.autenticacao.model.TokenEmitido;
import com.psnf.api.psnf.domain.autenticacao.port.out.AccessTokenPort;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** sub = id do usuário. Nada sensível vai no token: ele é só assinado, não criptografado (é legível). */
@Component
@RequiredArgsConstructor
public class JwtAccessTokenAdapter implements AccessTokenPort {

    private final JwtEncoder encoder;
    private final AuthProperties props;

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.EMISSOR)
                .subject(usuario.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(props.accessValidade()))
                .claim("email", usuario.getEmail())
                .build();
        String valor = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenEmitido(valor, props.accessValidade());
    }
}
