package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoRequest(
        @NotNull(message = "Tipo de pagamento é obrigatório (PIX, BOLETO ou TRANSFERENCIA)")
        EnumTipoPagamento tipoPagamento,
        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal valor,
        String agencia,
        String codigoBanco,
        String conta,
        String nomeReferencia,
        String chavePix,
        String boleto,
        /** data do pagamento (yyyy-MM-dd); se não vier, é a data de hoje */
        LocalDate dataReferencia
) {
}
