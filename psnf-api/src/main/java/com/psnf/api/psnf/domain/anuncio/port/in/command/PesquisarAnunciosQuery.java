package com.psnf.api.psnf.domain.anuncio.port.in.command;

import java.util.UUID;

/**
 * Filtros da vitrine/pesquisa de anúncios (somente anúncios ativos).
 *
 * @param termo       texto livre (opcional): busca no título e na descrição
 * @param categoriaId categoria (opcional)
 * @param pagina      começa em 0
 * @param tamanho     itens por página (1 a 100)
 * @param somenteSemExigenciaDeCnpj quem pesquisa não tem CNPJ: só anúncios liberados para pessoa física
 *                    (e os dele mesmo, {@code visitanteId})
 */
public record PesquisarAnunciosQuery(String termo, UUID categoriaId, int pagina, int tamanho,
                                     UUID visitanteId, boolean somenteSemExigenciaDeCnpj) {

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

    public PesquisarAnunciosQuery(String termo, UUID categoriaId, int pagina, int tamanho) {
        this(termo, categoriaId, pagina, tamanho, null, false);
    }

    /** a mesma pesquisa, considerando se quem pesquisa tem CNPJ */
    public PesquisarAnunciosQuery paraVisitante(UUID visitanteId, boolean temCnpj) {
        return new PesquisarAnunciosQuery(termo, categoriaId, pagina, tamanho, visitanteId, !temCnpj);
    }

    public boolean temTermo() {
        return termo != null;
    }
}
