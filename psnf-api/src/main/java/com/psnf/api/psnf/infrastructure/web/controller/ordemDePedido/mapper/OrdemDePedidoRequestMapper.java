package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.*;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request.*;

import java.util.UUID;

public class OrdemDePedidoRequestMapper {

    public static CadastrarOrdemDePedidoCommand toCommand(UUID vendedorId, OrdemDePedidoRequest request) {
        return new CadastrarOrdemDePedidoCommand(
                vendedorId,
                request.compradorId(),
                request.itens().stream()
                        .map(i -> new ItemPedidoCommand(i.anuncioId(), i.variacaoId(), i.quantidade(), i.valorUnidade()))
                        .toList()
        );
    }

    public static RegistrarPagamentoCommand toCommand(UUID ordemId, UUID usuarioId, PagamentoRequest r) {
        return new RegistrarPagamentoCommand(ordemId, usuarioId, r.tipoPagamento(), r.valor(), r.agencia(),
                r.codigoBanco(), r.conta(), r.nomeReferencia(), r.chavePix(), r.boleto());
    }

    public static AdicionarNotaFiscalCommand toCommand(UUID ordemId, UUID usuarioId, NotaFiscalRequest r) {
        return new AdicionarNotaFiscalCommand(ordemId, usuarioId, r.caminho());
    }

    public static RegistrarMensagemIncidenteCommand toCommand(UUID ordemId, UUID usuarioId, MensagemIncidenteRequest r) {
        return new RegistrarMensagemIncidenteCommand(ordemId, usuarioId, r.mensagem(), r.imagem());
    }
}
