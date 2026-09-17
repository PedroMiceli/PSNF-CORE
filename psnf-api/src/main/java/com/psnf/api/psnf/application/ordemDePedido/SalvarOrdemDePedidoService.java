package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.CadastrarOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.CadastrarOrdemDePedidoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SalvarOrdemDePedidoService implements CadastrarOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    public OrdemDePedido cadastrarOrdemDePedido(CadastrarOrdemDePedidoCommand cadastrarOrdemDePedidoCommand) {

        OrdemDePedido ordemDePedido = OrdemDePedido.criar(
                LocalDateTime.now(),
                null,
                null,
                );

        return ordemDePedidoRepositoryPort.save(ordemDePedido);
    }
}
