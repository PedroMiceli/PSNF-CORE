package com.psnf.api.psnf.domain.autenticacao.port.in;

import com.psnf.api.psnf.domain.autenticacao.model.ClienteInfo;
import com.psnf.api.psnf.domain.autenticacao.model.SessaoAutenticada;

public interface AutenticacaoUseCase {

    /** confere e-mail e senha e abre uma sessão nova */
    SessaoAutenticada entrar(String email, String senha, ClienteInfo cliente);

    /** troca o refresh token por um novo par de tokens (rotação) */
    SessaoAutenticada renovar(String refreshToken, ClienteInfo cliente);

    /** encerra a sessão deste refresh token (logout); token desconhecido é ignorado */
    void sair(String refreshToken);

    /** encerra todas as sessões do usuário (todos os dispositivos) */
    void sairDeTodos(java.util.UUID usuarioId);
}
