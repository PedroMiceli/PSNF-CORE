package com.psnf.api.psnf.domain.categoria.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Categoria extends BaseDomain {

    private final String nome;

    private Categoria(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String nome) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        this.nome = nome.trim();
    }

    public static Categoria criar(String nome) {
        return new Categoria(null, LocalDateTime.now(), null, null, nome);
    }

    public static Categoria restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String nome) {
        return new Categoria(id, dataCadastro, dataAlteracao, dataExcluido, nome);
    }

    public String getNome() {
        return nome;
    }
}
