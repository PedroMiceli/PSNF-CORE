package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class PagamentoJpa extends BaseEntity {

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
    private OrdemDePedidoJpa ordemDePedido;

    public PagamentoJpa(UUID id, int agencia, int codigoBanco, int conta, String nomeReferencia, String chavePix, String boleto, float valor, EnumTipoPagamento tipoPagamento, OrdemDePedidoJpa ordemDePedido) {
        this.setIdAndDate(id);
        this.agencia = agencia;
        this.codigoBanco = codigoBanco;
        this.conta = conta;
        this.nomeReferencia = nomeReferencia;
        this.chavePix = chavePix;
        this.boleto = boleto;
        this.valor = valor;
        this.tipoPagamento = tipoPagamento;
        this.ordemDePedido = ordemDePedido;
    }
}