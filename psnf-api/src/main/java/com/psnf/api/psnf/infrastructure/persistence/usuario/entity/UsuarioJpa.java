package com.psnf.api.psnf.infrastructure.persistence.usuario.entity;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
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
public class UsuarioJpa extends BaseEntity {

    private String nome;
    private String email;
    private String senha;
    private String secret2fa;
    private String imagem;
    private int telefone;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_entrega_id")
    private EnderecoJpa enderecoJpaDeEntrega;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_recebimento_id")
    private EnderecoJpa enderecoJpaDeRecebimento;

    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnuncioJpa> anuncios = new ArrayList<>();

    @OneToMany(mappedBy = "comprador")
    private List<OrdemDePedidoJpa> compras = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor")
    private List<OrdemDePedidoJpa> vendas = new ArrayList<>();

    public UsuarioJpa(String nome, String email, String senha, String secret2fa) {}

}
