package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotaFiscal extends BaseDomain {

    private final String caminho;

    private NotaFiscal(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String caminho) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.caminho = caminho;
    }

    public static NotaFiscal criar(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            throw new IllegalArgumentException("Caminho da nota fiscal é obrigatório");
        }
        return new NotaFiscal(null, LocalDateTime.now(), null, null, caminho);
    }

    public static NotaFiscal restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String caminho) {
        return new NotaFiscal(id, dataCadastro, dataAlteracao, dataExcluido, caminho);
    }

    public String getCaminho() { return caminho; }
}
