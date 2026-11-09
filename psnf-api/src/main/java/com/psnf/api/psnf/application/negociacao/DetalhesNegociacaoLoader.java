package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.model.NegociacaoDetalhes;
import com.psnf.api.psnf.domain.ordemDePedido.models.ResumoOrdem;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Carrega o que as telas do chat mostram, SEM N+1: anúncios e usuários de todas as conversas em uma busca cada,
 * ordens como resumos (uma consulta de projeção) e, na lista do hub, só a contagem de ordens por conversa.
 */
@Component
@RequiredArgsConstructor
class DetalhesNegociacaoLoader {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;

    /** Uma conversa aberta no chat: inclui os resumos das ordens entre os dois participantes. */
    NegociacaoDetalhes carregar(Negociacao n) {
        Map<UUID, Anuncio> anuncios = anuncioRepositoryPort.buscarPorIds(n.getAnuncioIds());
        Map<UUID, Usuario> usuarios = usuarioRepositoryPort.buscarPorIds(List.of(n.getCompradorId(), n.getVendedorId()));
        List<ResumoOrdem> ordens = ordemDePedidoRepositoryPort.resumosEntre(n.getCompradorId(), n.getVendedorId());
        long daConversa = ordens.stream().filter(o -> n.getId().equals(o.negociacaoId())).count();
        return montar(n, anuncios, usuarios, ordens, daConversa);
    }

    /** Lista do hub: várias conversas com 3 consultas no total (anúncios, usuários, contagem de ordens). */
    List<NegociacaoDetalhes> carregarLista(List<Negociacao> negociacoes) {
        if (negociacoes.isEmpty()) return List.of();
        Set<UUID> anuncioIds = new LinkedHashSet<>();
        Set<UUID> usuarioIds = new LinkedHashSet<>();
        negociacoes.forEach(n -> {
            anuncioIds.addAll(n.getAnuncioIds());
            usuarioIds.add(n.getCompradorId());
            usuarioIds.add(n.getVendedorId());
        });
        Map<UUID, Anuncio> anuncios = anuncioRepositoryPort.buscarPorIds(anuncioIds);
        Map<UUID, Usuario> usuarios = usuarioRepositoryPort.buscarPorIds(usuarioIds);
        Map<UUID, Long> ordens = ordemDePedidoRepositoryPort.contarPorNegociacao(negociacoes.stream().map(Negociacao::getId).toList());
        return negociacoes.stream()
                .map(n -> montar(n, anuncios, usuarios, List.of(), ordens.getOrDefault(n.getId(), 0L)))
                .toList();
    }

    private NegociacaoDetalhes montar(Negociacao n, Map<UUID, Anuncio> anuncios, Map<UUID, Usuario> usuarios,
                                      List<ResumoOrdem> ordens, long quantidadeOrdens) {
        List<Anuncio> daNegociacao = ordenados(n.getAnuncioIds(), anuncios);
        return new NegociacaoDetalhes(n, daNegociacao.getFirst(), daNegociacao,
                obrigatorio(usuarios, n.getCompradorId()), obrigatorio(usuarios, n.getVendedorId()), ordens, quantidadeOrdens);
    }

    /** na ordem da negociação (o de abordagem primeiro); um anúncio que sumiu cai na busca individual (404) */
    private List<Anuncio> ordenados(Collection<UUID> ids, Map<UUID, Anuncio> anuncios) {
        return ids.stream().map(id -> anuncios.containsKey(id) ? anuncios.get(id) : anuncioRepositoryPort.buscarPorId(id)).toList();
    }

    private Usuario obrigatorio(Map<UUID, Usuario> usuarios, UUID id) {
        Usuario u = usuarios.get(id);
        return u != null ? u : usuarioRepositoryPort.buscarPorId(id);
    }
}
