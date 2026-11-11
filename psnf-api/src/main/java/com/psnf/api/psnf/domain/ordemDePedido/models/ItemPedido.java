package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.anuncio.model.InformacoesProduto;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Item da ordem. O valor unitário, a variação e as informações do produto (SKU, NCM, garantia...)
 * são "congelados" no momento da compra: alterações posteriores no anúncio não mudam pedidos já feitos.
 * O valor unitário só muda por renegociação, enquanto o comprador não aceitou a ordem
 * ({@link OrdemDePedido#alterarValores}).
 */
public class ItemPedido extends BaseDomain {

    private final UUID anuncioId;
    private final String tituloAnuncio;
    private final String variacao;
    private final int quantidade;
    private BigDecimal valorUnidade;
    private final InformacoesProduto informacoes;

    private ItemPedido(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade, InformacoesProduto informacoes) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.anuncioId = anuncioId;
        this.tituloAnuncio = tituloAnuncio;
        this.variacao = variacao;
        this.quantidade = quantidade;
        this.valorUnidade = valorUnidade;
        this.informacoes = InformacoesProduto.ouVazio(informacoes);
    }

    public static ItemPedido criar(UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade) {
        return criar(anuncioId, tituloAnuncio, variacao, quantidade, valorUnidade, InformacoesProduto.VAZIO);
    }

    public static ItemPedido criar(UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade,
                                   InformacoesProduto informacoes) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        return new ItemPedido(null, LocalDateTime.now(), null, null, anuncioId, tituloAnuncio, variacao, quantidade, valorUnidade, informacoes);
    }

    public static ItemPedido restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, UUID anuncioId, String tituloAnuncio, String variacao, int quantidade, BigDecimal valorUnidade,
                                       InformacoesProduto informacoes) {
        return new ItemPedido(id, dataCadastro, dataAlteracao, dataExcluido, anuncioId, tituloAnuncio, variacao, quantidade, valorUnidade, informacoes);
    }

    /** novo valor combinado com o comprador (a ordem confere o status e quem pode alterar) */
    void alterarValorUnidade(BigDecimal novo) {
        if (novo == null || novo.signum() <= 0) throw new IllegalArgumentException("O valor unitário deve ser maior que zero");
        this.valorUnidade = novo;
    }

    public BigDecimal getSubtotal() {
        return valorUnidade.multiply(BigDecimal.valueOf(quantidade));
    }

    public UUID getAnuncioId() { return anuncioId; }
    public String getTituloAnuncio() { return tituloAnuncio; }
    public String getVariacao() { return variacao; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getValorUnidade() { return valorUnidade; }
    public InformacoesProduto getInformacoes() { return informacoes; }
}
