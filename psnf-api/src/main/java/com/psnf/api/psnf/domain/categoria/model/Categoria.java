package com.psnf.api.psnf.domain.categoria.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

public class Categoria extends BaseDomain {

    private String nome;

    public Categoria() {
    }

    public String getNome() {
        return nome;
    }
}
