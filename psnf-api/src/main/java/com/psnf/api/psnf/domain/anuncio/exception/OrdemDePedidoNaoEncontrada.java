package com.psnf.api.psnf.domain.anuncio.exception;

import java.util.UUID;

public class OrdemDePedidoNaoEncontrada extends RuntimeException {
    public OrdemDePedidoNaoEncontrada(UUID id) {
        super("Ordem de pedido não encontrada: " + id);
    }
}
