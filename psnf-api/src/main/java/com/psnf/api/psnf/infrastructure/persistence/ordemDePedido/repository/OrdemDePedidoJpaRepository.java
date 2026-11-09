package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository;

import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdemDePedidoJpaRepository extends JpaRepository<OrdemDePedidoJpa, UUID> {

    List<OrdemDePedidoJpa> findByCompradorIdOrderByDataCadastroDesc(UUID compradorId);

    List<OrdemDePedidoJpa> findByVendedorIdOrderByDataCadastroDesc(UUID vendedorId);

    List<OrdemDePedidoJpa> findByCompradorIdAndVendedorIdOrderByDataCadastroDesc(UUID compradorId, UUID vendedorId);
}
