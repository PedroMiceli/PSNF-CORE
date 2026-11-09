package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.MensagemIncidente;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.RegistrarMensagemIncidenteUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarMensagemIncidenteCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarMensagemIncidenteService implements RegistrarMensagemIncidenteUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    @Transactional
    public OrdemDePedido registrarMensagem(RegistrarMensagemIncidenteCommand command) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(command.ordemDePedidoId());
        MensagemIncidente mensagem = MensagemIncidente.criar(command.mensagem(), command.imagem(), command.usuarioId());
        ordem.registrarMensagemIncidente(command.usuarioId(), mensagem);
        return ordemDePedidoRepositoryPort.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido marcarLidas(UUID ordemDePedidoId, UUID usuarioId) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.marcarIncidentesLidos(usuarioId);
        return ordemDePedidoRepositoryPort.save(ordem);
    }
}
