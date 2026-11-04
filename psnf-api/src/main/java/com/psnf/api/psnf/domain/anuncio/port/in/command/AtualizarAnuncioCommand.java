package com.psnf.api.psnf.domain.anuncio.port.in.command;

import java.util.List;
import java.util.UUID;

public record AtualizarAnuncioCommand(
        UUID anuncioId,
        UUID usuarioId,
        String titulo,
        String descricao,
        UUID categoriaId,
        List<CadastrarValorAnuncioCommand> valores,
        List<CadastrarImagemAnuncioCommand> imagens,
        List<CadastrarVariacaoAnuncioCommand> variacoes
) {
    public AtualizarAnuncioCommand {
        if (anuncioId == null || usuarioId == null) {
            throw new IllegalArgumentException("Anúncio e usuário são obrigatórios");
        }
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
        variacoes = variacoes == null ? List.of() : variacoes;
    }
}
