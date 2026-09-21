package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class ItemPedidoJpa extends BaseEntity {

    private int quantidade;
    private BigDecimal valorUnidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedidoJpa ordemDePedido;

    public ItemPedidoJpa(AnuncioJpa anuncio, OrdemDePedidoJpa ordemDePedido, int quantidade, BigDecimal valorUnidade) {
        this.anuncio = anuncio;
        this.ordemDePedido = ordemDePedido;
        this.quantidade = quantidade;
        this.valorUnidade = valorUnidade;
    }
}