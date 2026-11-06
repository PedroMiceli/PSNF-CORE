package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.negociacao.model.enums.TipoMensagemNegociacao;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private LocalDateTime ultimaMensagemEm;

    private Negociacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                       UUID anuncioId, UUID compradorId, UUID vendedorId, List<MensagemNegociacao> mensagens,
                       LocalDateTime ultimaMensagemEm) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.anuncioId = anuncioId;
        this.compradorId = compradorId;
        this.vendedorId = vendedorId;
        this.mensagens = new ArrayList<>(mensagens == null ? List.of() : mensagens);
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
                anuncio.getId(), compradorId, anuncio.getVendedor(), List.of(), null);
        n.adicionar(MensagemNegociacao.criar(compradorId, textoAbordagem(anuncio.getTitulo(), nomeVendedor), TipoMensagemNegociacao.ABORDAGEM));
        return n;
    }

    public static Negociacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                       UUID anuncioId, UUID compradorId, UUID vendedorId, List<MensagemNegociacao> mensagens,
                                       LocalDateTime ultimaMensagemEm) {
        return new Negociacao(id, dataCadastro, dataAlteracao, dataExcluido, anuncioId, compradorId, vendedorId, mensagens, ultimaMensagemEm);
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
    public LocalDateTime getUltimaMensagemEm() { return ultimaMensagemEm; }
}
