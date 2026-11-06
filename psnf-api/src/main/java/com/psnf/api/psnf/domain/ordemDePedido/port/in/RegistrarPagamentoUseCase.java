package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarPagamentoCommand;

import java.util.UUID;

public interface RegistrarPagamentoUseCase {

    OrdemDePedido registrarPagamento(RegistrarPagamentoCommand command);

    /** O comprador exclui um pagamento registrado por engano (antes do envio do pedido). */
    OrdemDePedido removerPagamento(UUID ordemDePedidoId, UUID usuarioId, UUID pagamentoId);
}
