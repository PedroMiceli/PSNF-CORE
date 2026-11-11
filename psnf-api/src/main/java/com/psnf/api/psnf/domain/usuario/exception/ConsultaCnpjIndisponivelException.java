package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.ServicoExternoIndisponivelException;

/** A consulta de CNPJ (BrasilAPI) não respondeu. HTTP 503 na consulta avulsa; no cadastro, não bloqueia. */
public class ConsultaCnpjIndisponivelException extends ServicoExternoIndisponivelException {
    public ConsultaCnpjIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
