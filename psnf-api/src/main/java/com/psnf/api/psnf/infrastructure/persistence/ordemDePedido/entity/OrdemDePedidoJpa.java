package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.entity.NegociacaoJpa;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Relações:
 *  - N:1 Usuario (comprador)
 *  - N:1 Usuario (vendedor)
 *  - 1:N ItemPedido / Pagamento / MensagemIncidente / NotaFiscal (dono do ciclo de vida: cascade + orphanRemoval)
 *  - 1:1 Entrega (opcional: descrição e código de envio)
 *  - N:1 Negociacao (opcional: chat em que a ordem foi combinada)
 */
@Entity
@Table(name = "ordem_de_pedido")
@NoArgsConstructor
@Setter
@Getter
public class OrdemDePedidoJpa extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigo;

    private LocalDate previsaoDeEntrega;

    /** até quando comprador e vendedor já viram as mensagens de incidente */
    private LocalDateTime incidentesLidosCompradorEm;
    private LocalDateTime incidentesLidosVendedorEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumStatusOrdemDePedido status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private UsuarioJpa comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private UsuarioJpa vendedor;

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoJpa> itens = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCadastro ASC")
    private List<PagamentoJpa> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCadastro ASC")
    private List<MensagemIncidenteJpa> mensagensIncidente = new ArrayList<>();

    @OneToMany(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCadastro ASC")
    private List<NotaFiscalJpa> notasFiscais = new ArrayList<>();

    @OneToOne(mappedBy = "ordemDePedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EntregaJpa entrega;

    /** negociação (chat) em que a ordem foi combinada; opcional */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacao_id")
    private NegociacaoJpa negociacao;
}
