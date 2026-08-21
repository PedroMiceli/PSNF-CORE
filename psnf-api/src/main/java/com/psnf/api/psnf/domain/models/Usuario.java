package com.psnf.api.psnf.domain.models;

import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Usuario extends BaseEntity{

    private String nome;
    private String email;
    private String senha;
    private String secret2fa;
    private String imagem;
    private int telefone;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_entrega_id")
    private Endereco enderecoDeEntrega;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_recebimento_id")
    private Endereco enderecoDeRecebimento;

    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anuncio> anuncios = new ArrayList<>();

    @OneToMany(mappedBy = "compradorOrdemDePedido")
    private List<OrdemDePedido> compras = new ArrayList<>();

    @OneToMany(mappedBy = "vendedorOrdemDePedido")
    private List<OrdemDePedido> vendas = new ArrayList<>();

    public Usuario(String nome, String email, String senha, String secret2fa) {}

}
