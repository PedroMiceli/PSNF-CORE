package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarPagamentoCommand(
        UUID ordemDePedidoId,
        UUID usuarioId,
        EnumTipoPagamento tipoPagamento,
        BigDecimal valor,
        String agencia,
        String codigoBanco,
        String conta,
        String nomeReferencia,
        String chavePix,
        String boleto
) {
    public RegistrarPagamentoCommand {
        if (ordemDePedidoId == null || usuarioId == null) {
            throw new IllegalArgumentException("Ordem de pedido e usuário são obrigatórios");
        }
    }
}
