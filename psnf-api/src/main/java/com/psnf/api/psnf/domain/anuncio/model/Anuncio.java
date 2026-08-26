package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Anuncio extends BaseDomain {

    private String titulo;
    private String descricao;
    private boolean ativo;
    private UUID categoria;
    private UUID vendedor;
    private List<Valor> valores = new ArrayList<>();
    private List<ImagemAnuncio> imagens = new ArrayList<>();
    private List<Variacao> variacoes = new ArrayList<>();

    private Anuncio(UUID id, String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor) {
        super(id, null,null,null);
        this.titulo = titulo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.categoria = categoria;
        this.vendedor = vendedor;
    }

    private Anuncio(UUID id, String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor, List<Valor> valores, List<ImagemAnuncio> imagens, List<Variacao> variacoes) {
        super(id, null,null,null);
        this.titulo = titulo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.categoria = categoria;
        this.vendedor = vendedor;
        this.valores = valores;
        this.imagens = imagens;
        this.variacoes = variacoes;
    }

    public static Anuncio criar(String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor) {
        return new Anuncio(null, titulo, descricao, ativo, categoria, vendedor);
    }

    public static Anuncio restaurar(UUID id,String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor, List<Valor> valores, List<ImagemAnuncio> imagens, List<Variacao> variacoes){
        return new Anuncio(id, titulo, descricao, ativo, categoria, vendedor,  valores, imagens, variacoes);
    }

    public List<ImagemAnuncio> getImagens() {
        return imagens;
    }

    public List<Variacao> getVariacoes() {
        return variacoes;
    }

    public List<Valor> getValores() {
        return valores;
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