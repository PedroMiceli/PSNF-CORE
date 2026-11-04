package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarPagamentoCommand;

public interface RegistrarPagamentoUseCase {

    OrdemDePedido registrarPagamento(RegistrarPagamentoCommand command);
}
