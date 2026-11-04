package com.psnf.api.psnf.domain.ordemDePedido.port.out;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.util.List;
import java.util.UUID;

public interface OrdemDePedidoRepositoryPort {

    OrdemDePedido save(OrdemDePedido ordemDePedido);

    OrdemDePedido buscarPorId(UUID ordemDePedidoId);

    List<OrdemDePedido> buscarPorComprador(UUID compradorId);

    List<OrdemDePedido> buscarPorVendedor(UUID vendedorId);
}
