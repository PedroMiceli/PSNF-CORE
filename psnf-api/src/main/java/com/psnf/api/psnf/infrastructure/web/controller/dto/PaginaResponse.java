package com.psnf.api.psnf.infrastructure.web.controller.dto;

import com.psnf.api.psnf.domain.shared.model.Pagina;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(
        List<T> itens,
        int pagina,
        int tamanho,
        long total,
        int totalPaginas
) {
    public static <D, T> PaginaResponse<T> from(Pagina<D> pagina, Function<D, T> conversor) {
        Pagina<T> convertida = pagina.map(conversor);
        return new PaginaResponse<>(convertida.itens(), convertida.pagina(), convertida.tamanho(),
                convertida.total(), convertida.totalPaginas());
    }
}
