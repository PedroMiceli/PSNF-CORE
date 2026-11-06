package com.psnf.api.psnf.domain.anuncio.exception;

import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;

public class NcmNaoEncontradoException extends RecursoNaoEncontradoException {
    public NcmNaoEncontradoException(String ncm) {
        super("NCM não encontrado: " + ncm);
    }
}
