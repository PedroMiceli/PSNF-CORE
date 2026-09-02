package com.psnf.api.psnf.domain.usuario.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Usuario extends BaseEntity {

    private String nome;
    private String email;
    private String senha;
    private String secret2fa;
    private String imagem;
    private int telefone;

    private Endereco enderecoDeEntrega;

    private Endereco enderecoDeRecebimento;

    private List<Anuncio> anuncios = new ArrayList<>();

    private List<OrdemDePedido> compras = new ArrayList<>();

    private List<OrdemDePedido> vendas = new ArrayList<>();

    public Usuario(String nome, String email, String senha, String secret2fa) {}

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getSecret2fa() {
        return secret2fa;
    }

    public String getImagem() {
        return imagem;
    }

    public int getTelefone() {
        return telefone;
    }

    public Endereco getEnderecoDeEntrega() {
        return enderecoDeEntrega;
    }

    public Endereco getEnderecoDeRecebimento() {
        return enderecoDeRecebimento;
    }

    public List<Anuncio> getAnuncios() {
        return anuncios;
    }

    public List<OrdemDePedido> getCompras() {
        return compras;
    }

    public List<OrdemDePedido> getVendas() {
        return vendas;
    }
}
