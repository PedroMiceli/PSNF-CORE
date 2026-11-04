package com.psnf.api.psnf.domain.categoria.port.in.command;

public record CadastrarCategoriaCommand(
        String nome
) {
    public CadastrarCategoriaCommand {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
    }
}
