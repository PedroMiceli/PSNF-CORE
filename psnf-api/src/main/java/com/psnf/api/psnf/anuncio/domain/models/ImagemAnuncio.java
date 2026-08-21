package com.psnf.api.psnf.anuncio.domain.models;

import com.psnf.api.psnf.domain.models.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class ImagemAnuncio extends BaseEntity {

    private String caminho;
    private boolean principal = false;

    private Anuncio anuncio;

    public ImagemAnuncio() {
    }

    public ImagemAnuncio(String caminho, boolean principal, Anuncio anuncio) {
        this.caminho = caminho;
        this.principal = principal;
        this.anuncio = anuncio;
    }
}
