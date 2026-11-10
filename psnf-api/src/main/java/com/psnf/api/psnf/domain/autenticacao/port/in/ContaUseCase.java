package com.psnf.api.psnf.domain.autenticacao.port.in;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

/** Confirmação de e-mail e recuperação de senha. */
public interface ContaUseCase {

    /** logo após o cadastro: envia o link de confirmação */
    void enviarConfirmacao(Usuario usuario);

    void confirmarEmail(String token);

    /** sempre "funciona" para quem chama: não revela se o e-mail existe ou já está confirmado */
    void reenviarConfirmacao(String email);

    /** sempre "funciona" para quem chama: não revela se o e-mail existe */
    void solicitarRedefinicaoSenha(String email);

    /** troca a senha e encerra todas as sessões abertas */
    void redefinirSenha(String token, String novaSenha);
}
