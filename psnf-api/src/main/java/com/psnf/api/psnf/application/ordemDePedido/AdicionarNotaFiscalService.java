package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.NotaFiscal;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.AdicionarNotaFiscalUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.AdicionarNotaFiscalCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdicionarNotaFiscalService implements AdicionarNotaFiscalUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    @Transactional
    public OrdemDePedido adicionarNotaFiscal(AdicionarNotaFiscalCommand command) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(command.ordemDePedidoId());
        ordem.adicionarNotaFiscal(command.usuarioId(), NotaFiscal.criar(command.caminho()));
        return ordemDePedidoRepositoryPort.save(ordem);
    }
}
