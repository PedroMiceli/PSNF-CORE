package com.psnf.api.psnf.domain.negociacao.model.enums;

public enum TipoMensagemNegociacao {
    /** Primeira mensagem, gerada ao iniciar a negociação: apresenta o produto de interesse. */
    ABORDAGEM,
    /** Mensagem escrita por um dos participantes. */
    TEXTO,
    /** Aviso automático: um participante incluiu outro anúncio do vendedor na negociação. */
    ANUNCIO_ADICIONADO,
    /** Aviso automático: um anúncio foi retirado da negociação. */
    ANUNCIO_REMOVIDO,
    /** Aviso automático: o vendedor gerou uma ordem de pedido a partir desta negociação. */
    ORDEM_GERADA
}
