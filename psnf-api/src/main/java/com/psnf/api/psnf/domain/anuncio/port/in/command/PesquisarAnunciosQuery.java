package com.psnf.api.psnf.domain.anuncio.port.in.command;

import java.util.UUID;

/**
 * Filtros da vitrine/pesquisa de anúncios (somente anúncios ativos).
 *
 * @param termo       texto livre (opcional): busca no título e na descrição
 * @param categoriaId categoria (opcional)
 * @param vendedorId  só os anúncios deste vendedor (opcional; usado na página do vendedor)
 * @param pagina      começa em 0
 * @param tamanho     itens por página (1 a 100)
 */
public record PesquisarAnunciosQuery(String termo, UUID categoriaId, UUID vendedorId, int pagina, int tamanho) {

    /** vitrine geral: anúncios de todos os vendedores */
    public PesquisarAnunciosQuery(String termo, UUID categoriaId, int pagina, int tamanho) {
        this(termo, categoriaId, null, pagina, tamanho);
    }

    public static final int TAMANHO_PADRAO = 24;
    public static final int TAMANHO_MAXIMO = 100;

    public PesquisarAnunciosQuery {
        if (pagina < 0) {
            throw new IllegalArgumentException("A página deve ser maior ou igual a zero");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O tamanho da página deve ser entre 1 e " + TAMANHO_MAXIMO);
        }
        termo = termo == null || termo.isBlank() ? null : termo.trim();
    }

    public boolean temTermo() {
        return termo != null;
    }
}
