package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.ordemDePedido.models.ResumoOrdem;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.List;

/**
 * Negociação com os anúncios e os participantes carregados (o que a tela do chat precisa mostrar).
 *
 * @param anuncio  o anúncio de abordagem
 * @param anuncios todos os anúncios da negociação (o de abordagem primeiro)
 * @param ordens   ordens entre os dois participantes; as que saíram desta conversa têm negociacaoId igual ao dela
 */
public record NegociacaoDetalhes(Negociacao negociacao, Anuncio anuncio, List<Anuncio> anuncios, Usuario comprador, Usuario vendedor,
                                 /** resumos das ordens entre este comprador e este vendedor (vazio na lista do hub) */
                                 List<ResumoOrdem> ordens,
                                 /** ordens geradas a partir desta conversa */
                                 long quantidadeOrdens) {
}
