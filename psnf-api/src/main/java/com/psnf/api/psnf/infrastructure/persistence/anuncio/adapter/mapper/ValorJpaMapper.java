package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ValorJpa;

public class ValorJpaMapper {

    public static ValorJpa toEntity(Valor valor, AnuncioJpa anuncioJpa) {
        ValorJpa valorJpa = new ValorJpa(valor.getId(), valor.getValorUnidade(), valor.getQuantidadeMinima());
        valorJpa.setAnuncio(anuncioJpa);
        return valorJpa;
    }

    public static Valor toDomain(ValorJpa valorJpa) {
        return new Valor(valorJpa.getId(),valorJpa.getValorUnidade(), valorJpa.getQuantidadeMinima());
    }
}
