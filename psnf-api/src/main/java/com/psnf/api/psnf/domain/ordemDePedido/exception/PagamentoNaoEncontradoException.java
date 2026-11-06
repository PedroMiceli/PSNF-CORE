package com.psnf.api.psnf.domain.ordemDePedido.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class PagamentoNaoEncontradoException extends RecursoNaoEncontradoException {
    public PagamentoNaoEncontradoException(UUID id) {
        super("Pagamento não encontrado nesta ordem: " + id);
    }
}
