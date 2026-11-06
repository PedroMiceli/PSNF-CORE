package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Variação do anúncio (ex.: "Azul - M"). SKU e GTIN próprios são opcionais;
 * quando informados, substituem os do anúncio no item da ordem de pedido.
 */
public class Variacao extends BaseDomain {

    private final String variacao;
    private final String sku;
    private final String gtin;

    private Variacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                     String variacao, String sku, String gtin) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.variacao = variacao;
        this.sku = CodigosProduto.sku(sku);
        this.gtin = CodigosProduto.gtin(gtin);
    }

    public static Variacao criar(String variacao) {
        return criar(variacao, null, null);
    }

    public static Variacao criar(String variacao, String sku, String gtin) {
        return new Variacao(null, LocalDateTime.now(), null, null, variacao, sku, gtin);
    }

    public static Variacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                     String variacao, String sku, String gtin) {
        return new Variacao(id, dataCadastro, dataAlteracao, dataExcluido, variacao, sku, gtin);
    }

    public String getVariacao() { return variacao; }
    public String getSku() { return sku; }
    public String getGtin() { return gtin; }
}
