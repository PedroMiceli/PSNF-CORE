package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detalhes da entrega informados pelo vendedor.
 * Relações:
 *  - 1:1 OrdemDePedido (uma entrega por ordem)
 */
@Entity
@Table(name = "entrega")
@NoArgsConstructor
@Setter
@Getter
public class EntregaJpa extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 100)
    private String codigoRastreio;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false, unique = true)
    private OrdemDePedidoJpa ordemDePedido;
}
