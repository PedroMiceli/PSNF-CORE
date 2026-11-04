package com.psnf.api.psnf.domain.usuario.port.out;

public interface SenhaEncoderPort {

    String criptografar(String senhaPura);

    boolean confere(String senhaPura, String senhaCriptografada);
}
