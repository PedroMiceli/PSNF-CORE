package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Faixa de preço do anúncio: a partir de {@code quantidadeMinima} unidades, cada unidade custa {@code valorUnidade}.
 */
public class Valor extends BaseDomain {

    private final BigDecimal valorUnidade;
    private final int quantidadeMinima;

    private Valor(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, BigDecimal valorUnidade, int quantidadeMinima) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.valorUnidade = valorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    public static Valor criar(BigDecimal valorUnidade, int quantidadeMinima) {
        return new Valor(null, LocalDateTime.now(), null, null, valorUnidade, quantidadeMinima);
    }

    public static Valor restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, BigDecimal valorUnidade, int quantidadeMinima) {
        return new Valor(id, dataCadastro, dataAlteracao, dataExcluido, valorUnidade, quantidadeMinima);
    }

    public BigDecimal getValorUnidade() { return valorUnidade; }
    public int getQuantidadeMinima() { return quantidadeMinima; }
}
