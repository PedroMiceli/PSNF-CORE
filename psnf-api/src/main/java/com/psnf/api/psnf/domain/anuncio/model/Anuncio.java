package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.BaseDomain;

import java.util.UUID;


public class Anuncio extends BaseDomain {

    private String titulo;
    private String descricao;
    private boolean ativo;
    private UUID categoria;
    private UUID vendedor;

    private Anuncio(UUID id, String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor) {
        super(id, null,null,null);
        this.titulo = titulo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.categoria = categoria;
        this.vendedor = vendedor;
    }

    public static Anuncio criar(String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor) {
        return new Anuncio(null, titulo, descricao, ativo, categoria, vendedor);
    }

    public static Anuncio atualizar(UUID id,String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor) {
        return new Anuncio(id, titulo, descricao, ativo, categoria, vendedor);
    }

    public static Anuncio restaurar(UUID id,String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor){
        return new Anuncio(null, titulo, descricao, ativo, categoria, vendedor);
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getCategoria() {return categoria;}

    public UUID getVendedor() {
        return vendedor;
    }

}