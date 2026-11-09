package com.psnf.api.psnf.application.avaliacao;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.psnf.api.psnf.domain.avaliacao.model.Avaliacao;
import com.psnf.api.psnf.domain.avaliacao.model.AvaliacoesDoAnuncio;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;
import com.psnf.api.psnf.domain.avaliacao.port.in.AvaliarAnuncioUseCase;
import com.psnf.api.psnf.domain.avaliacao.port.out.AvaliacaoRepositoryPort;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Avaliações de produto. Só avalia quem COMPROU e RECEBEU o produto (ordem ENTREGUE com o anúncio),
 * e o vendedor nunca avalia o próprio anúncio.
 */
@Service
@RequiredArgsConstructor
public class AvaliarAnuncioService implements AvaliarAnuncioUseCase {

    private final AvaliacaoRepositoryPort avaliacaoRepositoryPort;
    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public AvaliacoesDoAnuncio listar(UUID anuncioId, UUID usuarioId) {
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(anuncioId);
        List<Avaliacao> lista = avaliacaoRepositoryPort.listarPorAnuncio(anuncioId);
        Map<UUID, Usuario> autores = new HashMap<>();
        lista.forEach(a -> autores.computeIfAbsent(a.getAutorId(), usuarioRepositoryPort::buscarPorId));
        Avaliacao minha = usuarioId == null ? null : lista.stream().filter(a -> a.pertenceA(usuarioId)).findFirst().orElse(null);
        String motivo = motivoNaoPodeAvaliar(anuncio, usuarioId);
        return new AvaliacoesDoAnuncio(ResumoAvaliacoes.de(lista), lista, autores, minha, motivo == null, motivo);
    }

    @Override
    @Transactional
    public AvaliacoesDoAnuncio avaliar(UUID anuncioId, UUID usuarioId, int nota, String texto, List<String> imagens) {
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(anuncioId);
        String motivo = motivoNaoPodeAvaliar(anuncio, usuarioId);
        if (motivo != null) throw new AcessoNegadoException(motivo);

        Optional<Avaliacao> existente = avaliacaoRepositoryPort.buscarPorAnuncioEAutor(anuncioId, usuarioId);
        Avaliacao avaliacao = existente.map(a -> { a.editar(nota, texto, imagens); return a; })
                .orElseGet(() -> Avaliacao.criar(anuncioId, usuarioId, nota, texto, imagens));
        avaliacaoRepositoryPort.save(avaliacao);
        return listar(anuncioId, usuarioId);
    }

    @Override
    @Transactional
    public AvaliacoesDoAnuncio remover(UUID anuncioId, UUID usuarioId) {
        Avaliacao a = avaliacaoRepositoryPort.buscarPorAnuncioEAutor(anuncioId, usuarioId)
                .orElseThrow(AvaliacaoNaoEncontradaException::new);
        avaliacaoRepositoryPort.remover(a.getId());
        return listar(anuncioId, usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, ResumoAvaliacoes> resumoPorAnuncios(Collection<UUID> anuncioIds) {
        return anuncioIds.isEmpty() ? Map.of() : avaliacaoRepositoryPort.resumoPorAnuncios(anuncioIds);
    }

    /** null quando pode avaliar; senão, a explicação para o usuário. */
    private String motivoNaoPodeAvaliar(Anuncio anuncio, UUID usuarioId) {
        if (usuarioId == null) return "Entre com a sua conta para avaliar";
        if (anuncio.pertenceA(usuarioId)) return "O vendedor não avalia o próprio anúncio";
        if (!ordemDePedidoRepositoryPort.existeCompraEntregue(usuarioId, anuncio.getId())) {
            return "Só quem comprou e recebeu este produto pode avaliar";
        }
        return null;
    }
}
