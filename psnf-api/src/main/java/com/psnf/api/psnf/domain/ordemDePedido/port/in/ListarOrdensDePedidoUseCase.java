package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.util.List;
import java.util.UUID;

public interface ListarOrdensDePedidoUseCase {

    List<OrdemDePedido> listarCompras(UUID usuarioId);

    List<OrdemDePedido> listarVendas(UUID usuarioId);

    /** Ordens (compras e vendas) com mensagens de incidente que o usuário ainda não viu. */
    List<OrdemDePedido> listarComMensagensNaoLidas(UUID usuarioId);
}
