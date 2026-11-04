package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.BuscarDetalhesOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarDetalhesOrdemDePedidoService implements BuscarDetalhesOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public OrdemDePedido buscarPorId(UUID ordemDePedidoId, UUID usuarioId) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.garantirParticipante(usuarioId);
        return ordem;
    }
}
