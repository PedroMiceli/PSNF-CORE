package com.psnf.api.psnf.domain.arquivo.exception;

/**
 * Arquivo vazio, de tipo não suportado ou acima do tamanho permitido (HTTP 400).
 */
public class ArquivoInvalidoException extends IllegalArgumentException {
    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
