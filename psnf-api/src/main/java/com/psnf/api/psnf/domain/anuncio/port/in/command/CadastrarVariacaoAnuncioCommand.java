package com.psnf.api.psnf.domain.anuncio.port.in.command;

public record CadastrarVariacaoAnuncioCommand(
        String variacao
) {
    public CadastrarVariacaoAnuncioCommand {
        if (variacao == null || variacao.isBlank()) {
            throw new IllegalArgumentException("Variação é obrigatória");
        }
    }
}