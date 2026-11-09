package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.List;

/**
 * Negociação com os anúncios e os participantes carregados (o que a tela do chat precisa mostrar).
 *
 * @param anuncio  o anúncio de abordagem
 * @param anuncios todos os anúncios da negociação (o de abordagem primeiro)
 * @param ordens   ordens entre os dois participantes; as que saíram desta conversa têm getNegociacaoId() igual ao dela
 */
public record NegociacaoDetalhes(Negociacao negociacao, Anuncio anuncio, List<Anuncio> anuncios, Usuario comprador, Usuario vendedor,
                                 /** ordens de pedido entre este comprador e este vendedor (a mais recente primeiro) */
                                 List<OrdemDePedido> ordens) {
}
