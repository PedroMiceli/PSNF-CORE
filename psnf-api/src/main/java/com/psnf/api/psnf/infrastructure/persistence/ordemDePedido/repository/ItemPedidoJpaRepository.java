package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository;

import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.ItemPedidoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemPedidoJpaRepository extends JpaRepository<ItemPedidoJpa, UUID> {

    List<ItemPedidoJpa> findByOrdemDePedidoId(UUID ordemDePedidoId);

    List<ItemPedidoJpa> findByAnuncioId(UUID anuncioId);
}
