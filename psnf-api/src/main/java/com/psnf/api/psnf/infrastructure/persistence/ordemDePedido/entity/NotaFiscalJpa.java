package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nota_fiscal")
@NoArgsConstructor
@Setter
@Getter
public class NotaFiscalJpa extends BaseEntity {

    @Column(nullable = false)
    private String caminho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedidoJpa ordemDePedido;
}
