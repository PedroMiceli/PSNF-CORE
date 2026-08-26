package com.psnf.api.psnf.domain.anuncio.port.in.command;

import java.math.BigDecimal;

public record CadastrarValorAnuncioCommand(
        BigDecimal valorUnidade,
        int quantidadeMinima
) {
    public CadastrarValorAnuncioCommand {
        if (valorUnidade == null || valorUnidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor unitário deve ser positivo");
        }
        if (quantidadeMinima < 0) {
            throw new IllegalArgumentException("Quantidade mínima não pode ser negativa");
        }
    }
}
