package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ImagemAnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ValorJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.VariacaoJpa;

import java.util.List;


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
    public static Anuncio toDomain(AnuncioJpa anuncio, List<ValorJpa> valores, List<ImagemAnuncioJpa> imagens, List<VariacaoJpa> variacoes) {
        return Anuncio.restaurar(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getDescricao(),
                anuncio.isAtivo(),
                anuncio.getCategoria(),
                anuncio.getVendedor(),
                valores.stream().map(ValorJpaMapper::toDomain).toList(),
                imagens.stream().map(ImagemJpaMapper::toDomain).toList(),
                variacoes.stream().map(VariacaoJpaMapper::toDomain).toList()
        );
    }

}
