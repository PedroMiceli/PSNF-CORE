package com.psnf.api.psnf.domain.autenticacao.port.out;

import com.psnf.api.psnf.domain.autenticacao.model.Email;

/**
 * Envio de e-mail. A implementação não bloqueia a requisição: envia em segundo plano, depois que a
 * transação for gravada (um e-mail com link para um token que não chegou ao banco seria inútil).
 */
public interface EnvioEmailPort {
    void enviar(Email email);
}
