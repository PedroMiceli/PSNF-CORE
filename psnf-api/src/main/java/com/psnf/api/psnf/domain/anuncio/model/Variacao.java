package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Variacao extends BaseDomain {

    private final String variacao;

    private Variacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String variacao) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.variacao = variacao;
    }

    public static Variacao criar(String variacao) {
        return new Variacao(null, LocalDateTime.now(), null, null, variacao);
    }

    public static Variacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String variacao) {
        return new Variacao(id, dataCadastro, dataAlteracao, dataExcluido, variacao);
    }

    public String getVariacao() { return variacao; }
}
