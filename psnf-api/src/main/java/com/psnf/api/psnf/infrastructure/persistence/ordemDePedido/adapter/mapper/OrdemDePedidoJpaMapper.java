package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper.UsuarioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

import java.util.UUID;
import java.util.function.Function;

public class OrdemDePedidoJpaMapper {

    /**
     * Transforma o domain para a entidade de infra.
     * As entidades referenciadas (usuários e anúncios) são resolvidas pelo adapter, normalmente como
     * referências (getReferenceById) para não precisar carregá-las.
     */
    public static OrdemDePedidoJpa toEntity(OrdemDePedido ordem,
                                           Function<UUID, UsuarioJpa> usuarioRef,
                                           Function<UUID, AnuncioJpa> anuncioRef) {
        OrdemDePedidoJpa jpa = new OrdemDePedidoJpa();
        jpa.restaurarBase(ordem.getId(), ordem.getDataCadastro(), ordem.getDataAlteracao(), ordem.getDataExcluido());
        jpa.setCodigo(ordem.getCodigo());
        jpa.setPrevisaoDeEntrega(ordem.getPrevisaoDeEntrega());
        jpa.setIncidentesLidosCompradorEm(ordem.getIncidentesLidosCompradorEm());
        jpa.setIncidentesLidosVendedorEm(ordem.getIncidentesLidosVendedorEm());
        jpa.setStatus(ordem.getStatus());
        jpa.setComprador(usuarioRef.apply(ordem.getComprador().getId()));
        jpa.setVendedor(usuarioRef.apply(ordem.getVendedor().getId()));

        ordem.getItens().forEach(i ->
                jpa.getItens().add(ItemPedidoJpaMapper.toEntity(i, anuncioRef.apply(i.getAnuncioId()), jpa)));
        ordem.getPagamentos().forEach(p ->
                jpa.getPagamentos().add(PagamentoJpaMapper.toEntity(p, jpa)));
        ordem.getMensagensIncidente().forEach(m ->
                jpa.getMensagensIncidente().add(MensagensIncidenteJpaMapper.toEntity(m, usuarioRef.apply(m.getUsuarioCriador()), jpa)));
        ordem.getNotasFiscais().forEach(n ->
                jpa.getNotasFiscais().add(NotaFiscalJpaMapper.toEntity(n, jpa)));
        return jpa;
    }

    //transforma o jpa para domain
    public static OrdemDePedido toDomain(OrdemDePedidoJpa jpa) {
        OrdemDePedido ordem = OrdemDePedido.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getCodigo(),
                jpa.getPrevisaoDeEntrega(),
                jpa.getStatus(),
                UsuarioJpaMapper.toDomain(jpa.getComprador()),
                UsuarioJpaMapper.toDomain(jpa.getVendedor()),
                jpa.getItens().stream().map(ItemPedidoJpaMapper::toDomain).toList(),
                jpa.getPagamentos().stream().map(PagamentoJpaMapper::toDomain).toList(),
                jpa.getMensagensIncidente().stream().map(MensagensIncidenteJpaMapper::toDomain).toList(),
                jpa.getNotasFiscais().stream().map(NotaFiscalJpaMapper::toDomain).toList()
        );
        ordem.restaurarLeituraIncidentes(jpa.getIncidentesLidosCompradorEm(), jpa.getIncidentesLidosVendedorEm());
        return ordem;
    }
}
