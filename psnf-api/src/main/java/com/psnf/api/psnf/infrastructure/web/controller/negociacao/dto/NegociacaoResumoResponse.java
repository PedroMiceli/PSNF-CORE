package com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto;

import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

/** Linha da lista "Negociações", do ponto de vista do usuário logado. */
public record NegociacaoResumoResponse(
        UUID id,
        /** COMPRADOR ou VENDEDOR: o papel do usuário logado nesta conversa */
        String papel,
        UUID anuncioId,
        String tituloAnuncio,
        String imagemAnuncio,
        UUID outroParticipanteId,
        String outroParticipanteNome,
        String ultimaMensagem,
        UUID ultimaMensagemAutorId,
        LocalDateTime ultimaMensagemEm,
        int quantidadeMensagens,
        /** produtos na negociação (o de abordagem + os incluídos depois) */
        int quantidadeAnuncios,
        /** ordens de pedido geradas a partir desta conversa */
        int quantidadeOrdens,
        /** mensagens da outra pessoa depois da última mensagem do usuário logado */
        int mensagensSemResposta
) {
    public static NegociacaoResumoResponse from(NegociacaoDetalhes d, UUID usuarioLogado) {
        Negociacao n = d.negociacao();
        boolean souVendedor = n.ehVendedor(usuarioLogado);
        Usuario outro = souVendedor ? d.comprador() : d.vendedor();
        var ultima = n.getUltimaMensagem();
        return new NegociacaoResumoResponse(n.getId(), souVendedor ? "VENDEDOR" : "COMPRADOR",
                d.anuncio().getId(), d.anuncio().getTitulo(), d.anuncio().getImagemPrincipal().map(ImagemAnuncio::getCaminho).orElse(null),
                outro.getId(), outro.getNome(),
                ultima.map(m -> m.getTexto()).orElse(null), ultima.map(m -> m.getAutorId()).orElse(null),
                n.getUltimaMensagemEm() != null ? n.getUltimaMensagemEm() : n.getDataCadastro(),
                n.getMensagens().size(),
                n.getAnuncioIds().size(),
                (int) d.ordens().stream().filter(o -> n.getId().equals(o.getNegociacaoId())).count(),
                n.mensagensSemResposta(usuarioLogado));
    }
}
