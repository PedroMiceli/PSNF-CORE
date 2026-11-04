package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.VariacaoJpa;

public class VariacaoJpaMapper {

    public static VariacaoJpa toEntity(Variacao variacao, AnuncioJpa anuncioJpa) {
        VariacaoJpa jpa = new VariacaoJpa();
        jpa.restaurarBase(variacao.getId(), variacao.getDataCadastro(), variacao.getDataAlteracao(), variacao.getDataExcluido());
        jpa.setVariacao(variacao.getVariacao());
        jpa.setAnuncio(anuncioJpa);
        return jpa;
    }

    public static Variacao toDomain(VariacaoJpa jpa) {
        return Variacao.restaurar(jpa.getId(), jpa.getDataCadastro(), jpa.getDataAlteracao(), jpa.getDataExcluido(), jpa.getVariacao());
    }
}
