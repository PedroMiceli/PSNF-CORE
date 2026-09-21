package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.ItemPedido;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.ItemPedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;

public class ItemPedidoJpaMapper {

    public static ItemPedidoJpa toEntity(ItemPedido item, AnuncioJpa anuncio, OrdemDePedidoJpa ordemDePedido) {
        return new ItemPedidoJpa(anuncio, ordemDePedido, item.getQuantidade(), item.getValorUnidade());
    }

    public static ItemPedido toDomain(ItemPedidoJpa jpa) {
        return ItemPedido.restaurar(
                jpa.getId(),
                jpa.getAnuncio().getId(),
                jpa.getOrdemDePedido().getId(),
                jpa.getQuantidade(),
                jpa.getValorUnidade()
        );
    }
}
