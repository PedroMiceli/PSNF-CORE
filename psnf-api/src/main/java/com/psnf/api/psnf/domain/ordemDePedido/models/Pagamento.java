package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


public class Pagamento extends BaseDomain {

    private int agencia;
    private int codigoBanco;
    private int conta;
    private String nomeReferencia;
    private String chavePix;
    private String boleto;
    private float valor;

    private EnumTipoPagamento tipoPagamento;
    private OrdemDePedido ordemDePedido;

    public Pagamento(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
    }

    public int getAgencia() {
        return agencia;
    }

    public int getCodigoBanco() {
        return codigoBanco;
    }

    public int getConta() {
        return conta;
    }

    public String getNomeReferencia() {
        return nomeReferencia;
    }

    public String getChavePix() {
        return chavePix;
    }

    public String getBoleto() {
        return boleto;
    }

    public float getValor() {
        return valor;
    }

    public EnumTipoPagamento getTipoPagamento() {
        return tipoPagamento;
    }

    public OrdemDePedido getOrdemDePedido() {
        return ordemDePedido;
    }
}
