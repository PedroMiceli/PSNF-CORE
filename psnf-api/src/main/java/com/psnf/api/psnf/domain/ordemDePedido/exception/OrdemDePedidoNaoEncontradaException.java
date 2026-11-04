package com.psnf.api.psnf.domain.ordemDePedido.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class OrdemDePedidoNaoEncontradaException extends RecursoNaoEncontradoException {
    public OrdemDePedidoNaoEncontradaException(UUID id) {
        super("Ordem de pedido não encontrada: " + id);
    }
}
