package com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.negociacao.model.MensagemNegociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.negociacao.model.enums.TipoMensagemNegociacao;
import com.psnf.api.psnf.domain.ordemDePedido.models.ResumoOrdem;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;
import com.psnf.api.psnf.domain.usuario.model.Endereco;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Conversa completa: cabeçalho (anúncios e participantes) e mensagens.
 * {@code anuncio} é o anúncio de abordagem; {@code anuncios} são todos os produtos da negociação
 * (o de abordagem primeiro), usados para montar a ordem de pedido.
 */
public record NegociacaoResponse(
        UUID id,
        LocalDateTime dataCadastro,
        AnuncioResumo anuncio,
        List<AnuncioResumo> anuncios,
        Participante comprador,
        Participante vendedor,
        List<Mensagem> mensagens,
        /** ordens de pedido entre comprador e vendedor; daNegociacao = gerada a partir desta conversa */
        List<OrdemResumo> ordens
) {
    public record OrdemResumo(UUID id, String codigo, EnumStatusOrdemDePedido status, BigDecimal valorTotal,
                              LocalDateTime dataCadastro, boolean pagamentoPendente, boolean daNegociacao) {
        static OrdemResumo from(ResumoOrdem o, UUID negociacaoId) {
            return new OrdemResumo(o.id(), o.codigo(), o.status(), o.valorTotal(), o.dataCadastro(),
                    o.pagamentoPendente(), negociacaoId.equals(o.negociacaoId()));
        }
    }

    public record AnuncioResumo(UUID id, String titulo, String imagem, BigDecimal precoMenor, int quantidadeMinima, boolean ativo) {
        static AnuncioResumo from(Anuncio a) {
            return new AnuncioResumo(a.getId(), a.getTitulo(), a.getImagemPrincipal().map(ImagemAnuncio::getCaminho).orElse(null),
                    a.getMenorValor(), a.getQuantidadeMinima(), a.isAtivo());
        }
    }

    /** Dados simples do participante, para o outro saber com quem está falando. */
    public record Participante(UUID id, String nome, String email, String telefone, String imagem,
                               String cidade, String uf, LocalDateTime membroDesde) {
        static Participante from(Usuario u) {
            Endereco e = u.getEnderecos().isEmpty() ? null : u.getEnderecos().getFirst();
            return new Participante(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(), u.getImagem(),
                    e == null ? null : e.getCidade(), e == null ? null : e.getUf(), u.getDataCadastro());
        }
    }

    public record Mensagem(UUID id, UUID autorId, String texto, TipoMensagemNegociacao tipo, UUID anuncioId, UUID ordemId,
                           LocalDateTime dataCadastro) {
        public static Mensagem from(MensagemNegociacao m) {
            return new Mensagem(m.getId(), m.getAutorId(), m.getTexto(), m.getTipo(), m.getAnuncioId(), m.getOrdemId(), m.getDataCadastro());
        }
    }

    public static NegociacaoResponse from(NegociacaoDetalhes d) {
        return new NegociacaoResponse(d.negociacao().getId(), d.negociacao().getDataCadastro(),
                AnuncioResumo.from(d.anuncio()), d.anuncios().stream().map(AnuncioResumo::from).toList(), Participante.from(d.comprador()), Participante.from(d.vendedor()),
                d.negociacao().getMensagens().stream().map(Mensagem::from).toList(),
                d.ordens().stream().map(o -> OrdemResumo.from(o, d.negociacao().getId())).toList());
    }
}
