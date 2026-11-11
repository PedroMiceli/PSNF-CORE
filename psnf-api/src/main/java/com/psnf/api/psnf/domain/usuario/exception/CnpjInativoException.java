package com.psnf.api.psnf.domain.usuario.exception;

import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;

/** A Receita informa o CNPJ como baixado, inapto, suspenso ou nulo (HTTP 422). */
public class CnpjInativoException extends RegraDeNegocioException {
    public CnpjInativoException(String situacao) {
        super("Este CNPJ consta como " + situacao + " na Receita Federal. Só CNPJs ativos podem ser cadastrados.");
    }
}
