package com.psnf.api.psnf.domain.anuncio.port.in.command;

/**
 * @param sku  opcional: SKU próprio da variação
 * @param gtin opcional: código de barras próprio da variação
 */
public record CadastrarVariacaoAnuncioCommand(
        String variacao,
        String sku,
        String gtin
) {
    public CadastrarVariacaoAnuncioCommand(String variacao) {
        this(variacao, null, null);
    }

    public CadastrarVariacaoAnuncioCommand {
        if (variacao == null || variacao.isBlank()) {
            throw new IllegalArgumentException("Variação é obrigatória");
        }
    }
}