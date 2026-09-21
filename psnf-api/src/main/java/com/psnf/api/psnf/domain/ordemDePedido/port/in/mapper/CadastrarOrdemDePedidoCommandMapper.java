package com.psnf.api.psnf.domain.ordemDePedido.port.in.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.ItemPedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.ItemPedidoCommand;

import java.util.List;

public class CadastrarOrdemDePedidoCommandMapper {
    public static List<ItemPedido> toItens(List<ItemPedidoCommand> itens) {
        return itens.stream()
                .map(i -> ItemPedido.criar(i.anuncioId(), i.quantidade(), i.valorUnidade()))
                .toList();
    }
}
