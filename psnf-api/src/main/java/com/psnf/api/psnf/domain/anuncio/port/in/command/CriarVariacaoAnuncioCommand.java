package com.psnf.api.psnf.domain.anuncio.port.in.command;

public record CriarVariacaoAnuncioCommand(
        String variacao
) {
    public CriarVariacaoAnuncioCommand {
        if (variacao == null || variacao.isBlank()) {
            throw new IllegalArgumentException("Variação é obrigatória");
        }
    }
}