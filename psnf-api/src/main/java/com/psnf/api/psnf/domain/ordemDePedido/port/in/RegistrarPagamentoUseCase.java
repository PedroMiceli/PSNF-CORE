package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarPagamentoCommand;

import java.util.UUID;

public interface RegistrarPagamentoUseCase {

    OrdemDePedido registrarPagamento(RegistrarPagamentoCommand command);

    /** O vendedor exclui uma forma de pagamento cadastrada por engano (antes do comprovante). */
    OrdemDePedido removerPagamento(UUID ordemDePedidoId, UUID usuarioId, UUID pagamentoId);

    /** O comprador anexa o comprovante de um pagamento. */
    OrdemDePedido anexarComprovante(UUID ordemDePedidoId, UUID usuarioId, UUID pagamentoId, String caminho);
}
