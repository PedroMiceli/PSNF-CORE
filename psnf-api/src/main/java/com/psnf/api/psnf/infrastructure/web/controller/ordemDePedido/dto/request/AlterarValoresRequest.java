package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Novos valores unitários dos itens (renegociação antes de o comprador aceitar). */
public record AlterarValoresRequest(@NotEmpty(message = "Informe ao menos um item") List<@Valid Item> itens) {

    public record Item(@NotNull(message = "Informe o item") UUID itemId,
                       @NotNull(message = "Informe o valor")
                       @DecimalMin(value = "0.01", message = "O valor unitário deve ser maior que zero")
                       @Digits(integer = 12, fraction = 2, message = "Valor inválido")
                       BigDecimal valorUnidade) {
    }
}
