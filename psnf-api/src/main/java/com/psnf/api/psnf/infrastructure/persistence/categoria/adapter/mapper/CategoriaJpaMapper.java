package com.psnf.api.psnf.infrastructure.persistence.categoria.adapter.mapper;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.infrastructure.persistence.categoria.entity.CategoriaJpa;

public class CategoriaJpaMapper {

    public static CategoriaJpa toEntity(Categoria categoria) {
        CategoriaJpa jpa = new CategoriaJpa();
        jpa.restaurarBase(categoria.getId(), categoria.getDataCadastro(), categoria.getDataAlteracao(), categoria.getDataExcluido());
        jpa.setNome(categoria.getNome());
        return jpa;
    }

    public static Categoria toDomain(CategoriaJpa jpa) {
        return Categoria.restaurar(jpa.getId(), jpa.getDataCadastro(), jpa.getDataAlteracao(), jpa.getDataExcluido(), jpa.getNome());
    }
}
