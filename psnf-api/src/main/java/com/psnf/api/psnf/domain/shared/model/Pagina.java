package com.psnf.api.psnf.domain.shared.model;

import java.util.List;
import java.util.function.Function;

/**
 * Uma página de resultados.
 *
 * @param pagina  número da página, começando em 0
 * @param tamanho quantidade máxima de itens por página
 * @param total   total de itens em todas as páginas
 */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long total) {

    public int totalPaginas() {
        return tamanho == 0 ? 0 : (int) Math.ceil((double) total / tamanho);
    }

    public <R> Pagina<R> map(Function<T, R> conversor) {
        return new Pagina<>(itens.stream().map(conversor).toList(), pagina, tamanho, total);
    }
}
