package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.AlterarValoresOrdemUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlterarValoresOrdemService implements AlterarValoresOrdemUseCase {

    private final OrdemDePedidoRepositoryPort ordens;

    @Override
    @Transactional
    public OrdemDePedido alterarValores(UUID ordemId, UUID usuarioId, Map<UUID, BigDecimal> valoresPorItem) {
        OrdemDePedido ordem = ordens.buscarPorId(ordemId);
        ordem.alterarValores(usuarioId, valoresPorItem);
        return ordens.save(ordem);
    }
}
