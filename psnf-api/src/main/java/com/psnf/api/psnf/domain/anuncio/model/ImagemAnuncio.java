package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class ImagemAnuncio extends BaseDomain {

    private final String caminho;
    private boolean principal;

    private ImagemAnuncio(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String caminho, boolean principal) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.caminho = caminho;
        this.principal = principal;
    }

    public static ImagemAnuncio criar(String caminho, boolean principal) {
        return new ImagemAnuncio(null, LocalDateTime.now(), null, null, caminho, principal);
    }

    public static ImagemAnuncio restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String caminho, boolean principal) {
        return new ImagemAnuncio(id, dataCadastro, dataAlteracao, dataExcluido, caminho, principal);
    }

    void definirPrincipal(boolean principal) {
        this.principal = principal;
    }

    public String getCaminho() { return caminho; }
    public boolean isPrincipal() { return principal; }
}
