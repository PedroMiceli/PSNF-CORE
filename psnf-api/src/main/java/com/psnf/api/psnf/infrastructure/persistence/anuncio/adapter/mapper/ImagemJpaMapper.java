package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ImagemAnuncioJpa;

public class ImagemJpaMapper {

    public static ImagemAnuncioJpa toEntity(ImagemAnuncio imagem, AnuncioJpa anuncioJpa) {
        ImagemAnuncioJpa jpa = new ImagemAnuncioJpa();
        jpa.restaurarBase(imagem.getId(), imagem.getDataCadastro(), imagem.getDataAlteracao(), imagem.getDataExcluido());
        jpa.setCaminho(imagem.getCaminho());
        jpa.setPrincipal(imagem.isPrincipal());
        jpa.setAnuncio(anuncioJpa);
        return jpa;
    }

    public static ImagemAnuncio toDomain(ImagemAnuncioJpa jpa) {
        return ImagemAnuncio.restaurar(jpa.getId(), jpa.getDataCadastro(), jpa.getDataAlteracao(), jpa.getDataExcluido(), jpa.getCaminho(), jpa.isPrincipal());
    }
}
