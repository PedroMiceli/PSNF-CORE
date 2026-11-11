package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface AlterarValoresOrdemUseCase {
    /** o vendedor renegocia o valor unitário dos itens antes de o comprador aceitar */
    OrdemDePedido alterarValores(UUID ordemId, UUID usuarioId, Map<UUID, BigDecimal> valoresPorItem);
}
