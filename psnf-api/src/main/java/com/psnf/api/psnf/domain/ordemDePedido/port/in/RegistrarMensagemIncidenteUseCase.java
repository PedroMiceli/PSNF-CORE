package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarMensagemIncidenteCommand;

public interface RegistrarMensagemIncidenteUseCase {

    OrdemDePedido registrarMensagem(RegistrarMensagemIncidenteCommand command);
}
