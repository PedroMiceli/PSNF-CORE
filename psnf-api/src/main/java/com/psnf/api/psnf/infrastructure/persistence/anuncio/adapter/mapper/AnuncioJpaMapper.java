package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;


public class AnuncioJpaMapper {

    //transforma o domain para a entidade de infra
    public static AnuncioJpa toEntity(Anuncio anuncio) {
        return new AnuncioJpa(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getDescricao(),
                anuncio.isAtivo(),
                anuncio.getCategoria(),
                anuncio.getVendedor()
        );
    }
    //transforma o jpa para domain
    public static Anuncio toDomain(AnuncioJpa anuncio) {
        return Anuncio.restaurar(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getDescricao(),
                anuncio.isAtivo(),
                anuncio.getCategoria(),
                anuncio.getVendedor()
        );
    }

}
