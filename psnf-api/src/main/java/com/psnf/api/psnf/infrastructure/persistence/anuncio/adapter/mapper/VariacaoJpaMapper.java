package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.VariacaoJpa;

public class VariacaoJpaMapper {

    public static VariacaoJpa toEntity(Variacao variacao, AnuncioJpa anuncioJpa) {
        VariacaoJpa variacaoJpa = new VariacaoJpa();
        variacaoJpa.setVariacao(variacao.getVariacao());
        variacaoJpa.setAnuncio(anuncioJpa);
        return variacaoJpa;
    }

    public static Variacao toDomain(VariacaoJpa variacaoJpa) {
        return new Variacao(variacaoJpa.getId(),variacaoJpa.getVariacao());
    }
}
