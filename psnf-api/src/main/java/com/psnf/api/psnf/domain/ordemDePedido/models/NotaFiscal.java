package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class NotaFiscal extends BaseEntity {

    private String caminho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedido ordemDePedido;
}
