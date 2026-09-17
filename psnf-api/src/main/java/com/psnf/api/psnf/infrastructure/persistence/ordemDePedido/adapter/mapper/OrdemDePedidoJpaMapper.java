package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper.UsuarioJpaMapper;

public class OrdemDePedidoJpaMapper {

    //transforma o domain para a entidade de infra
    public static OrdemDePedidoJpa toEntity(OrdemDePedido ordem) {
        return new OrdemDePedidoJpa();
    }
    //transforma o jpa para domain
    public static OrdemDePedido toDomain(OrdemDePedidoJpa ordem) {
        return OrdemDePedido.restaurar(
                ordem.getId(),
                ordem.getDataCadastro(),
                ordem.getDataAlteracao(),
                ordem.getDataExcluido(),
                ordem.getCodigo(),
                ordem.getPrevisaoDeEntrega(),
                UsuarioJpaMapper.toDomain(ordem.getComprador()),
                UsuarioJpaMapper.toDomain(ordem.getVendedor()),
                ordem.getPagamentos().stream().map(PagamentoJpaMapper::toDomain).toList(),
                ordem.getMensagensIncidente().stream().map(MensagensIncidenteJpaMapper::toDomain).toList(),
                ordem.getNotasFiscais().stream().map(NotaFiscalJpaMapper::toDomain).toList()
        );
    }
}
