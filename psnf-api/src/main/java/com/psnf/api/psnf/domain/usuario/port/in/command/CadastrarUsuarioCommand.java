package com.psnf.api.psnf.domain.usuario.port.in.command;

import java.util.List;

public record CadastrarUsuarioCommand(
        String email,
        String senha,
        String nome,
        String telefone,
        String imagem,
        List<EnderecoCommand> enderecos
) {

    public CadastrarUsuarioCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter ao menos 6 caracteres");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("É necessário informar ao menos um nome para a conta");
        }
        enderecos = enderecos == null ? List.of() : enderecos;
    }
}
