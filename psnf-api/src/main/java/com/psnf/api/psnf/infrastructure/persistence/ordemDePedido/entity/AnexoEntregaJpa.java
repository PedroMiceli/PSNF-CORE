package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Tabela criada na migração V9. */
@Entity
@Table(name = "anexo_entrega")
@NoArgsConstructor
@Getter
@Setter
public class AnexoEntregaJpa extends BaseEntity {

    @Column(nullable = false)
    private String caminho;

    @Column(name = "enviado_por_id", nullable = false)
    private UUID enviadoPor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedidoJpa ordemDePedido;
}
