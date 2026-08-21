package com.psnf.api.psnf.anuncio.domain.models;

import com.psnf.api.psnf.anuncio.adapters.outbound.entities.JPAValor;
import com.psnf.api.psnf.categoria.Categoria;
import com.psnf.api.psnf.domain.models.*;

import java.util.ArrayList;
import java.util.List;


public class Anuncio extends BaseEntity {

    private String titulo;
    private String descricao;
    private boolean ativo;

    private List<Valor> valores = new ArrayList<>();

    private List<ImagemAnuncio> imagens = new ArrayList<>();

    private Categoria categoria;

    private Usuario vendedor;

    private List<Variacao> variacoes = new ArrayList<>();

    public Anuncio() {
    }

    public Anuncio(String titulo, String descricao, boolean ativo, List<Valor> valores, List<ImagemAnuncio> imagens, Categoria categoria, Usuario vendedor, List<Variacao> variacoes) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.valores = valores;
        this.imagens = imagens;
        this.categoria = categoria;
        this.vendedor = vendedor;
        this.variacoes = variacoes;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<Valor> getValores() {
        return valores;
    }

    public void setValores(List<Valor> valores) {
        this.valores = valores;
    }

    public List<ImagemAnuncio> getImagens() {
        return imagens;
    }

    public void setImagens(List<ImagemAnuncio> imagens) {
        this.imagens = imagens;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public List<Variacao> getVariacoes() {
        return variacoes;
    }

    public void setVariacoes(List<Variacao> variacoes) {
        this.variacoes = variacoes;
    }
}