package com.psnf.api.psnf.domain.anuncio.port.in.command;

public record CadastrarImagemAnuncioCommand(
        String caminho,
        boolean principal
) {
    public CadastrarImagemAnuncioCommand {
        if (caminho == null || caminho.isBlank()) {
            throw new IllegalArgumentException("Caminho da imagem é obrigatório");
        }
    }
}
