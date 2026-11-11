package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.util.UUID;

/** Fotos/PDFs da entrega, anexados pelo vendedor como prova de saída ou de retirada. */
public interface AnexosEntregaUseCase {
    OrdemDePedido adicionar(UUID ordemId, UUID usuarioId, String caminho);

    OrdemDePedido remover(UUID ordemId, UUID usuarioId, UUID anexoId);
}
