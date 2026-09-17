package com.psnf.api.psnf.domain.ordemDePedido.port.in.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CadastrarOrdemDePedidoCommand(
        UUID compradorId,
        UUID vendedorId,
        LocalDate previsaoDeEntrega,
        List<PagamentoCommand> pagamentos

) {
}
