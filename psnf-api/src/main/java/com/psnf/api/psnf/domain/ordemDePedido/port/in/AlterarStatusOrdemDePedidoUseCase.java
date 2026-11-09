package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;

import java.time.LocalDate;
import java.util.UUID;

public interface AlterarStatusOrdemDePedidoUseCase {

    /** o comprador confere a ordem e aceita */
    OrdemDePedido aceitar(UUID ordemDePedidoId, UUID usuarioId);

    OrdemDePedido enviar(UUID ordemDePedidoId, UUID usuarioId, LocalDate previsaoDeEntrega, String descricaoEntrega, String codigoRastreio);

    /** O vendedor informa ou corrige a descrição da entrega e o código de envio (os dois opcionais). */
    OrdemDePedido registrarEntrega(UUID ordemDePedidoId, UUID usuarioId, String descricao, String codigoRastreio);

    OrdemDePedido confirmarEntrega(UUID ordemDePedidoId, UUID usuarioId);

    OrdemDePedido cancelar(UUID ordemDePedidoId, UUID usuarioId);
}
