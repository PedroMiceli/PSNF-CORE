package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resumo da ordem para listas (ex.: as ordens no chat). Vem de uma única consulta de projeção, sem carregar
 * itens, pagamentos, mensagens etc. — só o necessário para mostrar código, status, total e pendência.
 *
 * @param valorPago soma dos pagamentos com comprovante
 */
public record ResumoOrdem(UUID id, String codigo, EnumStatusOrdemDePedido status, LocalDateTime dataCadastro,
                          UUID negociacaoId, BigDecimal valorTotal, BigDecimal valorPago) {

    /** mesma regra de OrdemDePedido.isPagamentoPendente() */
    public boolean pagamentoPendente() {
        return status != EnumStatusOrdemDePedido.CANCELADO && valorPago.compareTo(valorTotal) < 0;
    }
}
