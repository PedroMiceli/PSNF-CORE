package com.psnf.api.psnf.domain.autenticacao.port.out;

import com.psnf.api.psnf.domain.autenticacao.model.TokenEmitido;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

/** Emite o token de acesso de curta duração (JWT assinado). */
public interface AccessTokenPort {
    TokenEmitido emitir(Usuario usuario);
}
