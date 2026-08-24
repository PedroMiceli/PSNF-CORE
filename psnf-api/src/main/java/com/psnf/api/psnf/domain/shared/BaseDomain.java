package com.psnf.api.psnf.domain.shared;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseDomain {

    private UUID id;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAlteracao;
    private LocalDateTime dataExcluido;

    public BaseDomain(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido) {
        this.id = id;
        this.dataCadastro = dataCadastro;
        this.dataAlteracao = dataAlteracao;
        this.dataExcluido = dataExcluido;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public LocalDateTime getDataExcluido() {
        return dataExcluido;
    }
}
