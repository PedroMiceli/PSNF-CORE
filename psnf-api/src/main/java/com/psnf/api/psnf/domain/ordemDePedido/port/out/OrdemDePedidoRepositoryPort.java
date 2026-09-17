package com.psnf.api.psnf.domain.ordemDePedido.port.out;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.util.UUID;

public interface OrdemDePedidoRepositoryPort {

    OrdemDePedido save(OrdemDePedido ordemDePedido);

    OrdemDePedido buscarPorId(UUID ordemDePedido);

}
