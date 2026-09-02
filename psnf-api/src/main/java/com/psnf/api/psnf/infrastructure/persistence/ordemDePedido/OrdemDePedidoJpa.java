package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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

}

