package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.InformacoesProduto;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.InformacoesProdutoJpa;

public class InformacoesProdutoJpaMapper {

    public static InformacoesProdutoJpa toEntity(InformacoesProduto i) {
        if (i == null || i.vazio()) return null;
        return new InformacoesProdutoJpa(i.sku(), i.gtin(), i.marca(), i.modelo(), i.condicao(), i.ncm(), i.cest(), i.origem(),
                i.unidadeComercial(), i.quantidadePorEmbalagem(), i.pesoLiquidoKg(), i.pesoBrutoKg(), i.comprimentoCm(),
                i.larguraCm(), i.alturaCm(), i.garantiaMeses(), i.garantiaResponsavel(), i.certificacoes());
    }

    /** O Hibernate devolve null quando todas as colunas estão vazias. */
    public static InformacoesProduto toDomain(InformacoesProdutoJpa j) {
        if (j == null) return InformacoesProduto.VAZIO;
        return new InformacoesProduto(j.getSku(), j.getGtin(), j.getMarca(), j.getModelo(), j.getCondicao(), j.getNcm(), j.getCest(),
                j.getOrigem(), j.getUnidadeComercial(), j.getQuantidadePorEmbalagem(), j.getPesoLiquidoKg(), j.getPesoBrutoKg(),
                j.getComprimentoCm(), j.getLarguraCm(), j.getAlturaCm(), j.getGarantiaMeses(), j.getGarantiaResponsavel(), j.getCertificacoes());
    }
}
