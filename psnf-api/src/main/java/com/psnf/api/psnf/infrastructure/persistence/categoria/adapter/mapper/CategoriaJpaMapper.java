package com.psnf.api.psnf.infrastructure.persistence.categoria.adapter.mapper;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.infrastructure.persistence.categoria.entity.CategoriaJpa;

public class CategoriaJpaMapper {

    public static Categoria toDomain(CategoriaJpa jpa) {
        return Categoria.restaurar(jpa.getId(), jpa.getDataCadastro(), jpa.getDataAlteracao(), jpa.getDataExcluido(), jpa.getNome());
    }
}
