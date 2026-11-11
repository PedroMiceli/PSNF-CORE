package com.psnf.api.psnf.domain.usuario.port.in.command;

import java.util.UUID;

public record AtualizarUsuarioCommand(
        UUID usuarioId,
        String nome,
        String telefone,
        String imagem,
        /* ausente mantém; vazio remove */
        String cnpj
) {
    public AtualizarUsuarioCommand {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
    }
}
