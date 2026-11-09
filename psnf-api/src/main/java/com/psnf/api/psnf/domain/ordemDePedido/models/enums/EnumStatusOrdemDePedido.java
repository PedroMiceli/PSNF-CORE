package com.psnf.api.psnf.domain.ordemDePedido.models.enums;

/**
 * AGUARDANDO_CONFIRMACAO -> ACEITO -> AGUARDANDO_PAGAMENTO -> PAGO -> ENVIADO -> ENTREGUE (ou CANCELADO)
 */
public enum EnumStatusOrdemDePedido {
    /** criada pelo vendedor: o comprador confere produtos, quantidades e valores */
    AGUARDANDO_CONFIRMACAO,
    /** o comprador aceitou: o vendedor cadastra as formas de pagamento */
    ACEITO,
    /** todo o valor está nas formas de pagamento: o comprador paga e anexa os comprovantes */
    AGUARDANDO_PAGAMENTO,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO
}
