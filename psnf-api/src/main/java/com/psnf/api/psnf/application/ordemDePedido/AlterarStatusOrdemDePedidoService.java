package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.AlterarStatusOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlterarStatusOrdemDePedidoService implements AlterarStatusOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    @Transactional
    public OrdemDePedido aceitar(UUID ordemDePedidoId, UUID usuarioId) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.aceitar(usuarioId);
        return ordemDePedidoRepositoryPort.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido enviar(UUID ordemDePedidoId, UUID usuarioId, LocalDate previsaoDeEntrega) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.enviar(usuarioId, previsaoDeEntrega);
        return ordemDePedidoRepositoryPort.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido confirmarEntrega(UUID ordemDePedidoId, UUID usuarioId) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.confirmarEntrega(usuarioId);
        return ordemDePedidoRepositoryPort.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido cancelar(UUID ordemDePedidoId, UUID usuarioId) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.cancelar(usuarioId);
        return ordemDePedidoRepositoryPort.save(ordem);
    }
}
