package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.ListarOrdensDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListarOrdensDePedidoService implements ListarOrdensDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<OrdemDePedido> listarCompras(UUID usuarioId) {
        return ordemDePedidoRepositoryPort.buscarPorComprador(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemDePedido> listarVendas(UUID usuarioId) {
        return ordemDePedidoRepositoryPort.buscarPorVendedor(usuarioId);
    }
}
