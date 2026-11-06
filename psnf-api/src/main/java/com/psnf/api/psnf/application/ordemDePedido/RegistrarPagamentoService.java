package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.Pagamento;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.RegistrarPagamentoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.RegistrarPagamentoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarPagamentoService implements RegistrarPagamentoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    @Override
    @Transactional
    public OrdemDePedido registrarPagamento(RegistrarPagamentoCommand command) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(command.ordemDePedidoId());

        Pagamento pagamento = Pagamento.criar(
                command.tipoPagamento(),
                command.valor(),
                command.agencia(),
                command.codigoBanco(),
                command.conta(),
                command.nomeReferencia(),
                command.chavePix(),
                command.boleto(),
                command.dataReferencia()
        );

        ordem.registrarPagamento(command.usuarioId(), pagamento);
        return ordemDePedidoRepositoryPort.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido removerPagamento(UUID ordemDePedidoId, UUID usuarioId, UUID pagamentoId) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.removerPagamento(usuarioId, pagamentoId);
        return ordemDePedidoRepositoryPort.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido anexarComprovante(UUID ordemDePedidoId, UUID usuarioId, UUID pagamentoId, String caminho) {
        OrdemDePedido ordem = ordemDePedidoRepositoryPort.buscarPorId(ordemDePedidoId);
        ordem.anexarComprovante(usuarioId, pagamentoId, caminho);
        return ordemDePedidoRepositoryPort.save(ordem);
    }
}
