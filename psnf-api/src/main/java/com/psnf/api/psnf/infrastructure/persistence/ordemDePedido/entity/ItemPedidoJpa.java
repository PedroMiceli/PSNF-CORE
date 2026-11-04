package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Relações:
 *  - N:1 OrdemDePedido
 *  - N:1 Anuncio
 * Título, variação e valor unitário são uma "foto" do anúncio no momento da compra.
 */
@Entity
@Table(name = "item_pedido")
@NoArgsConstructor
@Setter
@Getter
public class ItemPedidoJpa extends BaseEntity {

    private int quantidade;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorUnidade;

    private String tituloAnuncio;

    private String variacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private AnuncioJpa anuncio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedidoJpa ordemDePedido;
}
