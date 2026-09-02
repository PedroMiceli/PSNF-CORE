package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


public class NotaFiscal extends BaseDomain {

    private String caminho;

    private OrdemDePedido ordemDePedido;

    public NotaFiscal(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String caminho, OrdemDePedido ordemDePedido) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.caminho = caminho;
        this.ordemDePedido = ordemDePedido;
    }

    public String getCaminho() {
        return caminho;
    }

    public OrdemDePedido getOrdemDePedido() {
        return ordemDePedido;
    }
}
