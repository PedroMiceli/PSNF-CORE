package com.psnf.api.psnf.domain.autenticacao.port.out;

/**
 * Freio contra adivinhação de senha, por e-mail: depois de algumas senhas erradas seguidas, o e-mail
 * só pode tentar de novo a cada alguns minutos. Vale igual para e-mails cadastrados ou não, para a
 * resposta não revelar quem tem conta.
 */
public interface LimiteTentativasLoginPort {

    /** segundos até a próxima tentativa ser aceita; 0 = pode tentar agora */
    long esperaSegundos(String email);

    void registrarFalha(String email);

    /** senha certa: zera o contador */
    void registrarSucesso(String email);
}
