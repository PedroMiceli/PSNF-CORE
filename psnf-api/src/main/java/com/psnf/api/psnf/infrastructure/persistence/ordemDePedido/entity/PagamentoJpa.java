package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@NoArgsConstructor
@Setter
@Getter
public class PagamentoJpa extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumTipoPagamento tipoPagamento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    private String agencia;
    private String codigoBanco;
    private String conta;
    private String nomeReferencia;
    private String chavePix;
    private String boleto;

    private LocalDate dataReferencia;

    /** comprovante anexado pelo comprador (caminho do arquivo) */
    private String comprovante;
    private LocalDateTime dataComprovante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_de_pedido_id", nullable = false)
    private OrdemDePedidoJpa ordemDePedido;
}
