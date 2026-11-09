package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository;

import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdemDePedidoJpaRepository extends JpaRepository<OrdemDePedidoJpa, UUID> {

    List<OrdemDePedidoJpa> findByCompradorIdOrderByDataCadastroDesc(UUID compradorId);

    List<OrdemDePedidoJpa> findByVendedorIdOrderByDataCadastroDesc(UUID vendedorId);

    List<OrdemDePedidoJpa> findByCompradorIdAndVendedorIdOrderByDataCadastroDesc(UUID compradorId, UUID vendedorId);

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
