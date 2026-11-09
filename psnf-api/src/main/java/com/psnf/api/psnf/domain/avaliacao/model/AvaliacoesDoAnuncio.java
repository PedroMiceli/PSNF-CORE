package com.psnf.api.psnf.domain.avaliacao.model;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tudo o que a página do anúncio mostra sobre avaliações, do ponto de vista do usuário logado.
 *
 * @param autores        autores das avaliações (para exibir o nome)
 * @param minha          avaliação do usuário logado, se houver
 * @param podeAvaliar    o usuário logado pode publicar/editar a avaliação
 * @param motivoNaoPode  explicação quando não pode (ex.: ainda não recebeu o produto)
 */
public record AvaliacoesDoAnuncio(ResumoAvaliacoes resumo, List<Avaliacao> avaliacoes, Map<UUID, Usuario> autores,
                                  Avaliacao minha, boolean podeAvaliar, String motivoNaoPode) {
}
