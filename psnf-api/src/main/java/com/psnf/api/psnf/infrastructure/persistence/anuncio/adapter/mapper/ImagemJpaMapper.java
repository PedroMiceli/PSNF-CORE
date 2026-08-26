package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ImagemAnuncioJpa;

import java.util.UUID;

public class ImagemJpaMapper {

    public static ImagemAnuncioJpa toEntity(ImagemAnuncio imagem, UUID anuncioId) {
        return new ImagemAnuncioJpa(imagem.getId(), imagem.getCaminho(), imagem.isPrincipal(), anuncioId);
    }

    public static ImagemAnuncio toDomain(ImagemAnuncioJpa imagemJpa) {
        return new ImagemAnuncio(imagemJpa.getId(), imagemJpa.getCaminho(), imagemJpa.isPrincipal(), null);
    }
}
