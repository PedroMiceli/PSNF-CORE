package com.psnf.api.psnf.domain.autenticacao.port.out;

/** Valores aleatórios seguros para tokens opacos e o hash usado para guardá-los. */
public interface GeradorTokenPort {
    /** 256 bits aleatórios em base64url */
    String gerar();

    /** SHA-256 em hexadecimal */
    String hash(String token);
}
