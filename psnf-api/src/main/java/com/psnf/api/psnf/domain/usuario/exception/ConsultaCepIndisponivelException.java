package com.psnf.api.psnf.domain.usuario.exception;

/**
 * O serviço externo de CEP não respondeu (fora do ar, lento demais ou erro inesperado). HTTP 503.
 */
public class ConsultaCepIndisponivelException extends RuntimeException {
    public ConsultaCepIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
