package com.psnf.api.psnf.infrastructure.persistence.usuario.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class UsuarioJpa extends BaseEntity {

    private String nome;
    private String email;
    private String senha;
    private String secret2fa;
    private String imagem;
    private int telefone;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnderecoJpa> enderecos = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnuncioJpa> anuncios = new ArrayList<>();

    @OneToMany(mappedBy = "comprador")
    private List<OrdemDePedidoJpa> compras = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor")
    private List<OrdemDePedidoJpa> vendas = new ArrayList<>();

    public UsuarioJpa(UUID id) {
        this.setId(id);
    }

    public UsuarioJpa(UUID id, String nome, String email, String senha, String secret2fa, String imagem, int telefone,
                      List<EnderecoJpa> enderecos, List<AnuncioJpa> anuncios,
                      List<OrdemDePedidoJpa> compras, List<OrdemDePedidoJpa> vendas) {
        this.setId(id);
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.secret2fa = secret2fa;
        this.imagem = imagem;
        this.telefone = telefone;
        this.enderecos = enderecos;
        this.anuncios = anuncios;
        this.compras = compras;
        this.vendas = vendas;
    }

    public UsuarioJpa(UUID id, String nome, String email, String senha, String secret2fa, String imagem, int telefone) {
        this.setId(id);
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.secret2fa = secret2fa;
        this.imagem = imagem;
        this.telefone = telefone;
    }

    // método utilitário pra manter os dois lados sincronizados
    public void adicionarEndereco(EnderecoJpa endereco) {
        enderecos.add(endereco);
        endereco.setUsuario(this);
    }
}