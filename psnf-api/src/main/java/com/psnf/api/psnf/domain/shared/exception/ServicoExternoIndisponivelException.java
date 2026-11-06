package com.psnf.api.psnf.domain.shared.exception;

/**
 * Um serviço externo (BrasilAPI de CEP, NCM...) não respondeu: fora do ar, lento demais ou erro inesperado. HTTP 503.
 */
public class ServicoExternoIndisponivelException extends RuntimeException {
    public ServicoExternoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
