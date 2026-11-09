package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository;

import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrdemDePedidoJpaRepository extends JpaRepository<OrdemDePedidoJpa, UUID> {

    List<OrdemDePedidoJpa> findByCompradorIdOrderByDataCadastroDesc(UUID compradorId);

    List<OrdemDePedidoJpa> findByVendedorIdOrderByDataCadastroDesc(UUID vendedorId);

    /** [id, codigo, status, dataCadastro, negociacaoId, total dos itens, total pago com comprovante] */
    @Query("""
            select o.id, o.codigo, o.status, o.dataCadastro, o.negociacao.id,
                   (select coalesce(sum(i.valorUnidade * i.quantidade), 0) from ItemPedidoJpa i where i.ordemDePedido = o),
                   (select coalesce(sum(p.valor), 0) from PagamentoJpa p
                     where p.ordemDePedido = o and p.comprovante is not null and p.comprovante <> '')
            from OrdemDePedidoJpa o
            where o.comprador.id = :c and o.vendedor.id = :v
            order by o.dataCadastro desc
            """)
    List<Object[]> resumosEntre(@Param("c") UUID compradorId, @Param("v") UUID vendedorId);

    @Query("select o.negociacao.id, count(o) from OrdemDePedidoJpa o where o.negociacao.id in :ids group by o.negociacao.id")
    List<Object[]> contarPorNegociacao(@Param("ids") Collection<UUID> negociacaoIds);

    /** mesma regra de "não lida" da ordem: mensagem do outro lado depois da última leitura de quem pergunta */
    @Query("""
            select distinct o.id from OrdemDePedidoJpa o join o.mensagensIncidente m
            where m.usuarioCriador.id <> :u
              and ((o.comprador.id = :u and (o.incidentesLidosCompradorEm is null or m.dataCadastro > o.incidentesLidosCompradorEm))
                or (o.vendedor.id = :u and (o.incidentesLidosVendedorEm is null or m.dataCadastro > o.incidentesLidosVendedorEm)))
            """)
    List<UUID> idsComMensagensNaoLidas(@Param("u") UUID usuarioId);

    @Query("""
            select count(o) > 0 from OrdemDePedidoJpa o join o.itens i
            where o.comprador.id = :compradorId and i.anuncio.id = :anuncioId
              and o.status = com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido.ENTREGUE
            """)
    boolean existeCompraEntregue(@Param("compradorId") UUID compradorId, @Param("anuncioId") UUID anuncioId);
}
