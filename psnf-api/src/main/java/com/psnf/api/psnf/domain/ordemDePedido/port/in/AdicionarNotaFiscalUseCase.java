package com.psnf.api.psnf.domain.ordemDePedido.port.in;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.AdicionarNotaFiscalCommand;

public interface AdicionarNotaFiscalUseCase {

    OrdemDePedido adicionarNotaFiscal(AdicionarNotaFiscalCommand command);
}
