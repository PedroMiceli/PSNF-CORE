package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@NoArgsConstructor
@Setter
@Getter
public class OrdemDePedidoJpa extends BaseEntity {

    private String codigo;
    private LocalDate previsaoDeEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private UsuarioJpa comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private UsuarioJpa vendedor;

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoJpa> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MensagemIncidenteJpa> mensagensIncidente = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaFiscalJpa> notasFiscais = new ArrayList<>();

    public OrdemDePedidoJpa(UUID id) {
        this.setIdAndDate(id);
    }

    public OrdemDePedidoJpa(UUID id, String codigo, LocalDate previsaoDeEntrega, UsuarioJpa comprador, UsuarioJpa vendedor, List<PagamentoJpa> pagamentos, List<MensagemIncidenteJpa> mensagensIncidente, List<NotaFiscalJpa> notasFiscais) {
        this.setIdAndDate(id);
        this.codigo = codigo;
        this.previsaoDeEntrega = previsaoDeEntrega;
        this.comprador = comprador;
        this.vendedor = vendedor;
        this.pagamentos = pagamentos;
        this.mensagensIncidente = mensagensIncidente;
        this.notasFiscais = notasFiscais;
    }
}

