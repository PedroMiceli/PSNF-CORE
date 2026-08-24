package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.models.BaseEntity;


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

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public void setAnuncio(Anuncio anuncio) {
        this.anuncio = anuncio;
    }
}
