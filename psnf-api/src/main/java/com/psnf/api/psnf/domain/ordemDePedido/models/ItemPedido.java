package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Item da ordem. O valor unitário e a variação são "congelados" no momento da compra,
 * assim alterações posteriores no anúncio não mudam pedidos já feitos.
 */
public class ItemPedido extends BaseDomain {

    private final UUID anuncioId;
    private final String tituloAnuncio;
    private final String variacao;
    private final int quantidade;
    private final BigDecimal valorUnidade;

    private ItemPedido(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.anuncioId = anuncioId;
        this.tituloAnuncio = tituloAnuncio;
        this.variacao = variacao;
        this.quantidade = quantidade;
        this.valorUnidade = valorUnidade;
    }

    public static ItemPedido criar(UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        return new ItemPedido(null, LocalDateTime.now(), null, null, anuncioId, tituloAnuncio, variacao, quantidade, valorUnidade);
    }

    public static ItemPedido restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade) {
        return new ItemPedido(id, dataCadastro, dataAlteracao, dataExcluido, anuncioId, tituloAnuncio, variacao, quantidade, valorUnidade);
    }

    public BigDecimal getSubtotal() {
        return valorUnidade.multiply(BigDecimal.valueOf(quantidade));
    }

    public UUID getAnuncioId() { return anuncioId; }
    public String getTituloAnuncio() { return tituloAnuncio; }
    public String getVariacao() { return variacao; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getValorUnidade() { return valorUnidade; }
}
