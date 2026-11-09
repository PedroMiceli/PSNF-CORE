package com.psnf.api.psnf.infrastructure.web.controller.avaliacao.dto;

import com.psnf.api.psnf.domain.avaliacao.model.Avaliacao;
import com.psnf.api.psnf.domain.avaliacao.model.AvaliacoesDoAnuncio;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Avaliações do anúncio para a página do produto.
 *
 * @param porNota quantidade com 1, 2, 3, 4 e 5 estrelas
 */
public record AvaliacoesResponse(
        BigDecimal media,
        int total,
        int[] porNota,
        List<AvaliacaoResponse> avaliacoes,
        AvaliacaoResponse minha,
        boolean podeAvaliar,
        String motivoNaoPode
) {
    public record AvaliacaoResponse(UUID id, UUID autorId, String autorNome, int nota, String texto, List<String> imagens,
                                    LocalDateTime dataCadastro, LocalDateTime dataAlteracao) {
        static AvaliacaoResponse from(Avaliacao a, Usuario autor) {
            return new AvaliacaoResponse(a.getId(), a.getAutorId(), autor == null ? "Comprador" : autor.getNome(), a.getNota(),
                    a.getTexto(), a.getImagens(), a.getDataCadastro(), a.getDataAlteracao());
        }
    }

    public static AvaliacoesResponse from(AvaliacoesDoAnuncio d) {
        return new AvaliacoesResponse(d.resumo().media(), d.resumo().total(), d.resumo().porNota(),
                d.avaliacoes().stream().map(a -> AvaliacaoResponse.from(a, d.autores().get(a.getAutorId()))).toList(),
                d.minha() == null ? null : AvaliacaoResponse.from(d.minha(), d.autores().get(d.minha().getAutorId())),
                d.podeAvaliar(), d.motivoNaoPode());
    }
}
