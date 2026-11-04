package com.psnf.api.psnf.domain.categoria.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

import java.util.UUID;

public class CategoriaNaoEncontradaException extends RecursoNaoEncontradoException {
    public CategoriaNaoEncontradaException(UUID id) {
        super("Categoria não encontrada: " + id);
    }
}
