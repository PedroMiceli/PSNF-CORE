package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;


public class Variacao extends BaseDomain {

    private String variacao;

    private Anuncio anuncio;

    public Variacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String variacao, Anuncio anuncio) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.variacao = variacao;
        this.anuncio = anuncio;
    }

    public Variacao(UUID id, String variacao) {
        super(id, LocalDateTime.now(), null, null);
        this.variacao = variacao;
    }

    public Variacao(String variacao) {
        super(null, LocalDateTime.now(), null, null);
        this.variacao = variacao;
    }

    public static Variacao criar(String variacao) {
        return new Variacao(variacao);
    }

    public String getVariacao() {
        return variacao;
    }
}
