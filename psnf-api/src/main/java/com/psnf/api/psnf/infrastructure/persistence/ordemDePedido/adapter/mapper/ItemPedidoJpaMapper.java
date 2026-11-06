package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.ItemPedido;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.InformacoesProdutoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.ItemPedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;

public class ItemPedidoJpaMapper {

    public static ItemPedidoJpa toEntity(ItemPedido item, AnuncioJpa anuncio, OrdemDePedidoJpa ordemDePedido) {
        ItemPedidoJpa jpa = new ItemPedidoJpa();
        jpa.restaurarBase(item.getId(), item.getDataCadastro(), item.getDataAlteracao(), item.getDataExcluido());
        jpa.setQuantidade(item.getQuantidade());
        jpa.setValorUnidade(item.getValorUnidade());
        jpa.setTituloAnuncio(item.getTituloAnuncio());
        jpa.setVariacao(item.getVariacao());
        jpa.setInformacoes(InformacoesProdutoJpaMapper.toEntity(item.getInformacoes()));
        jpa.setAnuncio(anuncio);
        jpa.setOrdemDePedido(ordemDePedido);
        return jpa;
    }

    public static ItemPedido toDomain(ItemPedidoJpa jpa) {
        return ItemPedido.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getAnuncio().getId(),
                jpa.getTituloAnuncio(),
                jpa.getVariacao(),
                jpa.getQuantidade(),
                jpa.getValorUnidade(),
                InformacoesProdutoJpaMapper.toDomain(jpa.getInformacoes())
        );
    }
}
