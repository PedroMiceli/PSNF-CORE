package com.psnf.api.psnf.domain.ordemDePedido.port.out;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.ResumoOrdem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    /** Resumos das ordens entre este comprador e este vendedor (uma consulta), da mais recente para a mais antiga. */
    List<ResumoOrdem> resumosEntre(UUID compradorId, UUID vendedorId);

    /** Quantas ordens foram geradas a partir de cada negociação (uma consulta para todas). */
    Map<UUID, Long> contarPorNegociacao(Collection<UUID> negociacaoIds);

}
