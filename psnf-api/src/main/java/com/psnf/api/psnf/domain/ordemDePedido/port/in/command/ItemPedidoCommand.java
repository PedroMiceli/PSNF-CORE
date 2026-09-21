package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoCommand(
        UUID anuncioId,
        int quantidade,
        BigDecimal valorUnidade
) {}
