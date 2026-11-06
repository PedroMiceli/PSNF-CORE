package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

/** Negociação com o anúncio e os participantes carregados (o que a tela do chat precisa mostrar). */
public record NegociacaoDetalhes(Negociacao negociacao, Anuncio anuncio, Usuario comprador, Usuario vendedor) {
}
