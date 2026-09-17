package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.BuscarDetalhesOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarDetalhesOrdemDePedidoService implements BuscarDetalhesOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    public OrdemDePedido buscarPorId(UUID ordemDePedidoId) {
        return ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
    }
}
