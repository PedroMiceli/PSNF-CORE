package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.negociacao.model.enums.TipoMensagemNegociacao;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Conversa entre um comprador e o vendedor sobre um anúncio (o "anúncio de abordagem").
 * Existe no máximo uma negociação por comprador e anúncio; ela começa com uma mensagem
 * de abordagem que apresenta o produto de interesse.
 */
public class Negociacao extends BaseDomain {

    private final UUID anuncioId;
    private final UUID compradorId;
    private final UUID vendedorId;
    private final List<MensagemNegociacao> mensagens;
    /** outros anúncios do mesmo vendedor incluídos na conversa (além do anúncio de abordagem) */
    private final List<AnuncioNegociacao> anunciosAdicionais;
    private LocalDateTime ultimaMensagemEm;

    /** limite de produtos numa mesma negociação */
    public static final int MAXIMO_ANUNCIOS = 30;

    private Negociacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                       UUID anuncioId, UUID compradorId, UUID vendedorId, List<MensagemNegociacao> mensagens,
                       List<AnuncioNegociacao> anunciosAdicionais, LocalDateTime ultimaMensagemEm) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.anuncioId = anuncioId;
        this.compradorId = compradorId;
        this.vendedorId = vendedorId;
        this.mensagens = new ArrayList<>(mensagens == null ? List.of() : mensagens);
        this.anunciosAdicionais = new ArrayList<>(anunciosAdicionais == null ? List.of() : anunciosAdicionais);
        this.ultimaMensagemEm = ultimaMensagemEm;
    }

    /**
     * O comprador inicia a negociação a partir do anúncio.
     *
     * @param nomeVendedor usado na saudação da mensagem de abordagem
     */
    public static Negociacao iniciar(Anuncio anuncio, UUID compradorId, String nomeVendedor) {
        if (compradorId == null) throw new IllegalArgumentException("Comprador é obrigatório");
        if (anuncio.pertenceA(compradorId)) {
            throw new RegraDeNegocioException("Você não pode negociar o seu próprio anúncio");
        }
        if (!anuncio.isAtivo()) {
            throw new RegraDeNegocioException("Este anúncio está inativo e não aceita novas negociações");
        }
        Negociacao n = new Negociacao(null, LocalDateTime.now(), null, null,
                anuncio.getId(), compradorId, anuncio.getVendedor(), List.of(), List.of(), null);
        n.adicionar(MensagemNegociacao.criar(compradorId, textoAbordagem(anuncio.getTitulo(), nomeVendedor),
                TipoMensagemNegociacao.ABORDAGEM, anuncio.getId()));
        return n;
    }

    public static Negociacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                       UUID anuncioId, UUID compradorId, UUID vendedorId, List<MensagemNegociacao> mensagens,
                                       List<AnuncioNegociacao> anunciosAdicionais, LocalDateTime ultimaMensagemEm) {
        return new Negociacao(id, dataCadastro, dataAlteracao, dataExcluido, anuncioId, compradorId, vendedorId, mensagens,
                anunciosAdicionais, ultimaMensagemEm);
    }

    static String textoAbordagem(String tituloAnuncio, String nomeVendedor) {
        String saudacao = nomeVendedor == null || nomeVendedor.isBlank() ? "Olá!" : "Olá, " + nomeVendedor.strip() + "!";
        return saudacao + " Tenho interesse no produto \"" + tituloAnuncio + "\" e gostaria de negociar "
                + "quantidades, valores e condições de entrega. Pode me passar mais detalhes?";
    }

    public MensagemNegociacao enviarMensagem(UUID autorId, String texto) {
        garantirParticipante(autorId);
        MensagemNegociacao m = MensagemNegociacao.criar(autorId, texto, TipoMensagemNegociacao.TEXTO);
        adicionar(m);
        return m;
    }

    /**
     * Inclui outro anúncio do mesmo vendedor na negociação (comprador ou vendedor podem incluir).
     * Fica registrado no chat com uma mensagem automática que mostra o produto.
     */
    public void adicionarAnuncio(UUID usuarioId, Anuncio anuncio) {
        garantirParticipante(usuarioId);
        if (!anuncio.pertenceA(vendedorId)) {
            throw new RegraDeNegocioException("Só é possível incluir anúncios do vendedor desta negociação");
        }
        if (!anuncio.isAtivo()) {
            throw new RegraDeNegocioException("O anúncio \"" + anuncio.getTitulo() + "\" está inativo");
        }
        if (contemAnuncio(anuncio.getId())) {
            throw new RegraDeNegocioException("O anúncio \"" + anuncio.getTitulo() + "\" já está nesta negociação");
        }
        if (getAnuncioIds().size() >= MAXIMO_ANUNCIOS) {
            throw new RegraDeNegocioException("Uma negociação aceita no máximo " + MAXIMO_ANUNCIOS + " produtos");
        }
        anunciosAdicionais.add(AnuncioNegociacao.criar(anuncio.getId(), usuarioId));
        adicionar(MensagemNegociacao.criar(usuarioId, "Adicionou o produto \"" + anuncio.getTitulo() + "\" à negociação.",
                TipoMensagemNegociacao.ANUNCIO_ADICIONADO, anuncio.getId()));
    }

    /** Retira um anúncio incluído depois. O anúncio de abordagem não pode ser retirado. */
    public void removerAnuncio(UUID usuarioId, Anuncio anuncio) {
        garantirParticipante(usuarioId);
        if (anuncio.getId().equals(anuncioId)) {
            throw new RegraDeNegocioException("O produto que iniciou a negociação não pode ser retirado");
        }
        if (!anunciosAdicionais.removeIf(a -> a.getAnuncioId().equals(anuncio.getId()))) {
            throw new RegraDeNegocioException("O anúncio \"" + anuncio.getTitulo() + "\" não está nesta negociação");
        }
        adicionar(MensagemNegociacao.criar(usuarioId, "Retirou o produto \"" + anuncio.getTitulo() + "\" da negociação.",
                TipoMensagemNegociacao.ANUNCIO_REMOVIDO, anuncio.getId()));
    }

    /** Aviso no chat de que o vendedor gerou uma ordem de pedido a partir desta conversa. */
    public void registrarOrdemGerada(UUID vendedor, UUID ordemId, String codigo, BigDecimal valorTotal) {
        if (!ehVendedor(vendedor)) {
            throw new AcessoNegadoException("Apenas o vendedor da negociação gera ordens de pedido a partir dela");
        }
        String valor = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valorTotal);
        adicionar(MensagemNegociacao.criar(vendedor, "Gerou a ordem de pedido " + codigo + " (" + valor + ").",
                TipoMensagemNegociacao.ORDEM_GERADA).comOrdem(ordemId));
    }

    public boolean contemAnuncio(UUID id) {
        return anuncioId.equals(id) || anunciosAdicionais.stream().anyMatch(a -> a.getAnuncioId().equals(id));
    }

    /** Todos os produtos da negociação: o de abordagem primeiro, depois os incluídos, na ordem em que entraram. */
    public List<UUID> getAnuncioIds() {
        List<UUID> ids = new ArrayList<>();
        ids.add(anuncioId);
        anunciosAdicionais.forEach(a -> ids.add(a.getAnuncioId()));
        return ids;
    }

    private void adicionar(MensagemNegociacao m) {
        mensagens.add(m);
        ultimaMensagemEm = m.getDataCadastro();
    }

    public boolean participa(UUID usuarioId) {
        return compradorId.equals(usuarioId) || vendedorId.equals(usuarioId);
    }

    public void garantirParticipante(UUID usuarioId) {
        if (!participa(usuarioId)) {
            throw new AcessoNegadoException("Apenas o comprador e o vendedor participam desta negociação");
        }
    }

    public boolean ehVendedor(UUID usuarioId) {
        return vendedorId.equals(usuarioId);
    }

    /** Mensagens dos outros participantes depois da última mensagem deste usuário (ainda sem resposta dele). */
    public int mensagensSemResposta(UUID usuarioId) {
        int n = 0;
        for (int i = mensagens.size() - 1; i >= 0 && !mensagens.get(i).getAutorId().equals(usuarioId); i--) n++;
        return n;
    }

    public Optional<MensagemNegociacao> getUltimaMensagem() {
        return mensagens.isEmpty() ? Optional.empty() : Optional.of(mensagens.getLast());
    }

    public UUID getAnuncioId() { return anuncioId; }
    public UUID getCompradorId() { return compradorId; }
    public UUID getVendedorId() { return vendedorId; }
    public List<MensagemNegociacao> getMensagens() { return List.copyOf(mensagens); }
    public List<AnuncioNegociacao> getAnunciosAdicionais() { return List.copyOf(anunciosAdicionais); }
    public LocalDateTime getUltimaMensagemEm() { return ultimaMensagemEm; }
}
