package com.psnf.api.psnf.domain.anuncio.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Modelo de leitura (projeção) usado nas pesquisas de anúncios — vem do índice de busca.
 */
public record AnuncioResumo(
        UUID id,
        String titulo,
        String descricao,
        String imagemPrincipal,
        BigDecimal precoMenor,
        UUID categoriaId,
        String categoriaNome,
        UUID vendedorId,
        String vendedorNome
) {
}
