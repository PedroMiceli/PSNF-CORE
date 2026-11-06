package com.psnf.api.psnf.domain.anuncio.port.in.command;

import com.psnf.api.psnf.domain.anuncio.model.Caracteristica;
import com.psnf.api.psnf.domain.anuncio.model.InformacoesProduto;

import java.util.List;
import java.util.UUID;

public record CadastrarAnuncioCommand(
        UUID vendedorId,
        String titulo,
        String descricao,
        UUID categoriaId,
        List<CadastrarValorAnuncioCommand> valores,
        List<CadastrarImagemAnuncioCommand> imagens,
        List<CadastrarVariacaoAnuncioCommand> variacoes,
        /* opcionais: informações complementares do produto e ficha técnica */
        InformacoesProduto informacoes,
        List<Caracteristica> caracteristicas
) {
    public CadastrarAnuncioCommand {
        if (vendedorId == null) {
            throw new IllegalArgumentException("Vendedor é obrigatório");
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
        caracteristicas = caracteristicas == null ? List.of() : caracteristicas;
    }
}
