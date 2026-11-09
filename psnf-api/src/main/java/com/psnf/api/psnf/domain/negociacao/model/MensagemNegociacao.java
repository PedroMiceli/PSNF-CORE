package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.negociacao.model.enums.TipoMensagemNegociacao;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class MensagemNegociacao extends BaseDomain {

    public static final int TAMANHO_MAXIMO = 2000;

    private final UUID autorId;
    private final String texto;
    private final TipoMensagemNegociacao tipo;
    /** anúncio a que a mensagem se refere (abordagem, anúncio adicionado/removido); null nas mensagens de texto */
    private final UUID anuncioId;
    /** ordem de pedido citada (aviso de ordem gerada) */
    private UUID ordemId;

    private MensagemNegociacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                               UUID autorId, String texto, TipoMensagemNegociacao tipo, UUID anuncioId) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.autorId = autorId;
        this.texto = texto;
        this.tipo = tipo;
        this.anuncioId = anuncioId;
    }

    static MensagemNegociacao criar(UUID autorId, String texto, TipoMensagemNegociacao tipo) {
        return criar(autorId, texto, tipo, null);
    }

    static MensagemNegociacao criar(UUID autorId, String texto, TipoMensagemNegociacao tipo, UUID anuncioId) {
        if (autorId == null) throw new IllegalArgumentException("Autor da mensagem é obrigatório");
        String t = texto == null ? "" : texto.strip();
        if (t.isEmpty()) throw new IllegalArgumentException("Escreva uma mensagem");
        if (t.length() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A mensagem deve ter no máximo " + TAMANHO_MAXIMO + " caracteres");
        }
        return new MensagemNegociacao(null, LocalDateTime.now(), null, null, autorId, t, tipo, anuncioId);
    }

    public static MensagemNegociacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                               UUID autorId, String texto, TipoMensagemNegociacao tipo, UUID anuncioId) {
        return new MensagemNegociacao(id, dataCadastro, dataAlteracao, dataExcluido, autorId, texto, tipo, anuncioId);
    }

    public UUID getAutorId() { return autorId; }
    public String getTexto() { return texto; }
    public TipoMensagemNegociacao getTipo() { return tipo; }
    public UUID getAnuncioId() { return anuncioId; }
    public UUID getOrdemId() { return ordemId; }

    /** Usado ao criar o aviso de ordem gerada e pela persistência. */
    public MensagemNegociacao comOrdem(UUID ordemId) {
        this.ordemId = ordemId;
        return this;
    }
}
