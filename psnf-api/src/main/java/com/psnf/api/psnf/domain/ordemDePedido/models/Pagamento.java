package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Pagamento extends BaseDomain {

    private final EnumTipoPagamento tipoPagamento;
    private final BigDecimal valor;
    private final String agencia;
    private final String codigoBanco;
    private final String conta;
    private final String nomeReferencia;
    private final String chavePix;
    private final String boleto;
    /** data a que o pagamento se refere (ex.: quando foi ou será pago); informada por quem registra */
    private final LocalDate dataReferencia;

    private Pagamento(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                      EnumTipoPagamento tipoPagamento, BigDecimal valor, String agencia, String codigoBanco, String conta,
                      String nomeReferencia, String chavePix, String boleto, LocalDate dataReferencia) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.tipoPagamento = tipoPagamento;
        this.valor = valor;
        this.agencia = agencia;
        this.codigoBanco = codigoBanco;
        this.conta = conta;
        this.nomeReferencia = nomeReferencia;
        this.chavePix = chavePix;
        this.boleto = boleto;
        this.dataReferencia = dataReferencia;
    }

    public static Pagamento criar(EnumTipoPagamento tipoPagamento, BigDecimal valor, String agencia, String codigoBanco, String conta,
                                  String nomeReferencia, String chavePix, String boleto) {
        return criar(tipoPagamento, valor, agencia, codigoBanco, conta, nomeReferencia, chavePix, boleto, null);
    }

    /** @param dataReferencia data do pagamento; se não informada, é a data de hoje */
    public static Pagamento criar(EnumTipoPagamento tipoPagamento, BigDecimal valor, String agencia, String codigoBanco, String conta,
                                  String nomeReferencia, String chavePix, String boleto, LocalDate dataReferencia) {
        if (tipoPagamento == null) {
            throw new IllegalArgumentException("Tipo de pagamento é obrigatório");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
        }
        switch (tipoPagamento) {
            case PIX -> exigir(chavePix, "Chave PIX é obrigatória para pagamento via PIX");
            case BOLETO -> exigir(boleto, "Código do boleto é obrigatório para pagamento via boleto");
            case TRANSFERENCIA -> {
                exigir(codigoBanco, "Código do banco é obrigatório para transferência");
                exigir(agencia, "Agência é obrigatória para transferência");
                exigir(conta, "Conta é obrigatória para transferência");
            }
        }
        LocalDateTime agora = LocalDateTime.now();
        return new Pagamento(null, agora, null, null, tipoPagamento, valor, agencia, codigoBanco, conta, nomeReferencia, chavePix, boleto,
                dataReferencia != null ? dataReferencia : agora.toLocalDate());
    }

    public static Pagamento restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                      EnumTipoPagamento tipoPagamento, BigDecimal valor, String agencia, String codigoBanco, String conta,
                                      String nomeReferencia, String chavePix, String boleto) {
        return restaurar(id, dataCadastro, dataAlteracao, dataExcluido, tipoPagamento, valor, agencia, codigoBanco, conta, nomeReferencia,
                chavePix, boleto, null);
    }

    public static Pagamento restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                      EnumTipoPagamento tipoPagamento, BigDecimal valor, String agencia, String codigoBanco, String conta,
                                      String nomeReferencia, String chavePix, String boleto, LocalDate dataReferencia) {
        // pagamentos registrados antes deste campo: usa a data em que foram registrados
        LocalDate data = dataReferencia != null ? dataReferencia : dataCadastro == null ? null : dataCadastro.toLocalDate();
        return new Pagamento(id, dataCadastro, dataAlteracao, dataExcluido, tipoPagamento, valor, agencia, codigoBanco, conta, nomeReferencia,
                chavePix, boleto, data);
    }

    private static void exigir(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public EnumTipoPagamento getTipoPagamento() { return tipoPagamento; }
    public BigDecimal getValor() { return valor; }
    public String getAgencia() { return agencia; }
    public String getCodigoBanco() { return codigoBanco; }
    public String getConta() { return conta; }
    public String getNomeReferencia() { return nomeReferencia; }
    public String getChavePix() { return chavePix; }
    public String getBoleto() { return boleto; }
    public LocalDate getDataReferencia() { return dataReferencia; }
}
