package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptSenhaEncoderAdapter implements SenhaEncoderPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String criptografar(String senhaPura) {
        return encoder.encode(senhaPura);
    }

    @Override
    public boolean confere(String senhaPura, String senhaCriptografada) {
        return encoder.matches(senhaPura, senhaCriptografada);
    }
}
