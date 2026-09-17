package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;


public class ImagemAnuncio extends BaseDomain {

    private String caminho;
    private boolean principal = false;
    private Anuncio anuncio;

    public ImagemAnuncio() {
    }

    public ImagemAnuncio(UUID id, String caminho, boolean principal, Anuncio anuncio) {
        super(id, null,null,null);
        this.caminho = caminho;
        this.principal = principal;
        this.anuncio = anuncio;
    }

    public ImagemAnuncio(String caminho, boolean principal) {
        super(null, LocalDateTime.now(),null,null);
        this.caminho = caminho;
        this.principal = principal;
    }

    public static ImagemAnuncio criar(String caminho, boolean principal) {
        return new ImagemAnuncio(caminho, principal);
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
