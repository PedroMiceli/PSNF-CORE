package com.psnf.api.psnf.domain.usuario.port.in.command;

public record CadastrarUsuarioCommand(
        String email,
        String senha,
        String nome,
        int telefone
) {

    public CadastrarUsuarioCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (senha == null) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar ao menos um nome para a conta");
        }
    }
}
