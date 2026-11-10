package com.psnf.api.psnf.domain.autenticacao.model;

/** De onde veio o login: fica registrado na sessão (para listar dispositivos e investigar abusos). */
public record ClienteInfo(String ip, String userAgent) {
    public ClienteInfo {
        if (userAgent != null && userAgent.length() > 255) userAgent = userAgent.substring(0, 255);
        if (ip != null && ip.length() > 64) ip = ip.substring(0, 64);
    }
}
