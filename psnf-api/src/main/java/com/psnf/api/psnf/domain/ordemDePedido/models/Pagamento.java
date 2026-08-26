package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Pagamento extends BaseEntity {

    private int agencia;
    private int codigoBanco;
    private int conta;
    private String nomeReferencia;
    private String chavePix;
    private String boleto;
    private float valor;

    @Enumerated(EnumType.STRING)
    private EnumTipoPagamento tipoPagamento;

    @ManyToOne
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedido ordemDePedido;
}
