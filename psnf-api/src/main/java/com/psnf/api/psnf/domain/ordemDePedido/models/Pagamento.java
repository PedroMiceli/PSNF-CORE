package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

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
    private UUID ordemDePedido;

    public Pagamento(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                     int agencia, int codigoBanco, int conta, String nomeReferencia, String chavePix,
                     String boleto, float valor, EnumTipoPagamento tipoPagamento, UUID ordemDePedido) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.agencia = agencia;
        this.codigoBanco = codigoBanco;
        this.conta = conta;
        this.nomeReferencia = nomeReferencia;
        this.chavePix = chavePix;
        this.boleto = boleto;
        this.valor = valor;
        this.tipoPagamento = tipoPagamento;
        this.ordemDePedido = ordemDePedido;
    }

    public static Pagamento criar(LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                  int agencia, int codigoBanco, int conta, String nomeReferencia, String chavePix,
                                  String boleto, float valor, EnumTipoPagamento tipoPagamento, UUID ordemDePedido) {

        return new Pagamento(
                null,
                dataCadastro,
                dataAlteracao,
                dataExcluido,
                agencia,
                codigoBanco,
                conta,
                nomeReferencia,
                chavePix,
                boleto,
                valor,
                tipoPagamento,
                ordemDePedido
        );
    }

    public static Pagamento restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                      int agencia, int codigoBanco, int conta, String nomeReferencia, String chavePix,
                                      String boleto, float valor, EnumTipoPagamento tipoPagamento, UUID ordemDePedido) {

        return new Pagamento(
                id,
                dataCadastro,
                dataAlteracao,
                dataExcluido,
                agencia,
                codigoBanco,
                conta,
                nomeReferencia,
                chavePix,
                boleto,
                valor,
                tipoPagamento,
                ordemDePedido
        );
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

    public UUID getOrdemDePedido() {
        return ordemDePedido;
    }
}
