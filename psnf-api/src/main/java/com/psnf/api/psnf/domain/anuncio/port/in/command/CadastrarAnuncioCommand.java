package com.psnf.api.psnf.domain.anuncio.port.in.command;

import java.util.List;
import java.util.UUID;

public record CadastrarAnuncioCommand(
        String titulo,
        String descricao,
        UUID categoriaId,
        List<CadastrarValorAnuncioCommand> valores,
        List<CadastrarImagemAnuncioCommand> imagens,
        List<CriarVariacaoAnuncioCommand> variacoes
) {
    public CadastrarAnuncioCommand {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (categoriaId == null) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar ao menos um valor");
        }
        if (imagens == null || imagens.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar ao menos uma imagem");
        }
    }
}




