package com.psnf.api.psnf.domain.autenticacao.model;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

/**
 * Resultado de um login ou renovação.
 *
 * @param refreshToken null quando a renovação não troca o refresh token (renovação simultânea de outra aba)
 */
public record SessaoAutenticada(Usuario usuario, TokenEmitido accessToken, TokenEmitido refreshToken) {
}
