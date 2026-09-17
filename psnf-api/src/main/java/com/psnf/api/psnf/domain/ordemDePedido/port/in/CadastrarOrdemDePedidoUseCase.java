package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.CadastrarOrdemDePedidoCommand;

public interface CadastrarOrdemDePedidoUseCase {

    OrdemDePedido cadastrarOrdemDePedido(CadastrarOrdemDePedidoCommand cadastrarOrdemDePedidoCommand);
}
