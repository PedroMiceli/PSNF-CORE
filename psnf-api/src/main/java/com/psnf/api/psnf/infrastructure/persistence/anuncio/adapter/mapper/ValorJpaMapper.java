package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ValorJpa;

public class ValorJpaMapper {

    public static ValorJpa toEntity(Valor valor, AnuncioJpa anuncioJpa) {
        ValorJpa jpa = new ValorJpa();
        jpa.restaurarBase(valor.getId(), valor.getDataCadastro(), valor.getDataAlteracao(), valor.getDataExcluido());
        jpa.setValorUnidade(valor.getValorUnidade());
        jpa.setQuantidadeMinima(valor.getQuantidadeMinima());
        jpa.setAnuncio(anuncioJpa);
        return jpa;
    }

    public static Valor toDomain(ValorJpa jpa) {
        return Valor.restaurar(jpa.getId(), jpa.getDataCadastro(), jpa.getDataAlteracao(), jpa.getDataExcluido(), jpa.getValorUnidade(), jpa.getQuantidadeMinima());
    }
}
