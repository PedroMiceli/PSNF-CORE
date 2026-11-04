package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param variacaoId    opcional — variação do anúncio escolhida (ex.: cor/tamanho)
 * @param valorUnidade  valor unitário negociado entre vendedor e comprador
 */
public record ItemPedidoCommand(
        UUID anuncioId,
        UUID variacaoId,
        int quantidade,
        BigDecimal valorUnidade
) {
    public ItemPedidoCommand {
        if (anuncioId == null) {
            throw new IllegalArgumentException("Anúncio do item é obrigatório");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (valorUnidade == null || valorUnidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor unitário deve ser maior que zero");
        }
    }
}
