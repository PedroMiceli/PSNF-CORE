package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.util.UUID;

public interface BuscarDetalhesOrdemDePedidoUseCase {

    OrdemDePedido buscarPorId(UUID ordemDePedidoId);
}
