package com.psnf.api.psnf.domain.usuario.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Usuario extends BaseDomain {

    private String nome;
    private String email;
    private String senha;
    private String secret2fa;
    private String imagem;
    private int telefone;

    private List<Endereco> enderecos;

    private List<Anuncio> anuncios = new ArrayList<>();

    List<OrdemDePedido> compras = new ArrayList<>();

    private List<OrdemDePedido> vendas = new ArrayList<>();

    public Usuario(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExclusao, String nome, String email, String senha, String secret2fa, String imagem, int telefone, List<Endereco> enderecos, List<Anuncio> anuncios, List<OrdemDePedido> compras, List<OrdemDePedido> vendas) {
        super(id,dataCadastro, dataAlteracao, dataExclusao);
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

    public static Usuario criar(LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExclusao, String nome, String email, String senha, String secret2fa, String imagem, int telefone, List<Endereco> enderecos) {
        return new Usuario(
                null,
                dataCadastro,
                dataAlteracao,
                dataExclusao,
                nome,
                email,
                senha,
                secret2fa,
                imagem,
                telefone,
                enderecos,
                null,
                null,
                null
        );
    }

    public static Usuario restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExclusao, String nome, String email, String senha, String secret2fa, String imagem, int telefone, List<Endereco> enderecos, List<Anuncio> anuncios, List<OrdemDePedido> compras, List<OrdemDePedido> vendas){
        return new Usuario(
                id,
                dataCadastro,
                dataAlteracao,
                dataExclusao,
                nome,
                email,
                senha,
                secret2fa,
                imagem,
                telefone,
                enderecos,
                anuncios,
                compras,
                vendas
        );
    }

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

    public List<Endereco> getEnderecos() {
        return enderecos;
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
