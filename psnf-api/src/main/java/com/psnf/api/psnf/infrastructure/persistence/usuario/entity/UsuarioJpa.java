package com.psnf.api.psnf.infrastructure.persistence.usuario.entity;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Relações:
 *  - 1:N Endereco  (dono do ciclo de vida: cascade + orphanRemoval)
 *  - 1:N Anuncio   (como vendedor — lado inverso, sem cascade)
 *  - 1:N OrdemDePedido como comprador / como vendedor (lado inverso, sem cascade)
 */
@Entity
@Table(name = "usuario")
@NoArgsConstructor
@Getter
@Setter
public class UsuarioJpa extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String secret2fa;
    private String imagem;
    private String telefone;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnderecoJpa> enderecos = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor")
    private List<AnuncioJpa> anuncios = new ArrayList<>();

    @OneToMany(mappedBy = "comprador")
    private List<OrdemDePedidoJpa> compras = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor")
    private List<OrdemDePedidoJpa> vendas = new ArrayList<>();
}
