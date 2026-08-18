package com.psnf.api.psnf.domain.models;

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
public class OrdemDePedido extends BaseEntity{

    private String codigo;
    private LocalDate previsaoDeEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MensagemIncidente> mensagensIncidente = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaFiscal> notasFiscais = new ArrayList<>();

}
