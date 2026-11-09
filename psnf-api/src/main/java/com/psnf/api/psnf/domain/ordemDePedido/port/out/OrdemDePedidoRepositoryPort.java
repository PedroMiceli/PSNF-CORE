package com.psnf.api.psnf.domain.ordemDePedido.port.out;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.util.List;
import java.util.UUID;

public interface OrdemDePedidoRepositoryPort {

    OrdemDePedido save(OrdemDePedido ordemDePedido);

    OrdemDePedido buscarPorId(UUID ordemDePedidoId);

    List<OrdemDePedido> buscarPorComprador(UUID compradorId);

    List<OrdemDePedido> buscarPorVendedor(UUID vendedorId);

    /** Ids das ordens com mensagens de incidente do outro lado que o usuário ainda não viu (consulta agregada). */
    List<UUID> idsComMensagensNaoLidas(UUID usuarioId);

    /** O usuário comprou e recebeu o anúncio (ordem ENTREGUE com um item dele). */
    boolean existeCompraEntregue(UUID compradorId, UUID anuncioId);

    /** Ordens entre este comprador e este vendedor, da mais recente para a mais antiga. */
    List<OrdemDePedido> buscarPorCompradorEVendedor(UUID compradorId, UUID vendedorId);
}
