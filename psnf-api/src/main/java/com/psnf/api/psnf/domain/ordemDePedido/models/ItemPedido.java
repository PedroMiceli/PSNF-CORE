package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.util.UUID;

public class ItemPedido extends BaseDomain {

    private UUID anuncioId;
    private int quantidade;
    private BigDecimal valorUnidade;

    private ItemPedido(UUID id, UUID anuncioId, int quantidade, BigDecimal valorUnidade) {
        super(id, null, null, null);
        this.anuncioId = anuncioId;
        this.quantidade = quantidade;
        this.valorUnidade = valorUnidade;
    }

    public static ItemPedido criar(UUID anuncioId, int quantidade, BigDecimal valorUnidade) {
        return new ItemPedido(null, anuncioId, quantidade, valorUnidade);
    }

    public static ItemPedido restaurar(UUID id, UUID anuncioId, int quantidade, BigDecimal valorUnidade) {
        return new ItemPedido(id, anuncioId, quantidade, valorUnidade);
    }

    public BigDecimal getSubtotal() {
        return valorUnidade.multiply(BigDecimal.valueOf(quantidade));
    }

    public UUID getAnuncioId() { return anuncioId; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getValorUnidade() { return valorUnidade; }
}