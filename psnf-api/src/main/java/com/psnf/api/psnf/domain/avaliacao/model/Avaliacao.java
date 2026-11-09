package com.psnf.api.psnf.domain.avaliacao.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Análise de um produto feita por um comprador: nota de 1 a 5 estrelas, texto curto e até 3 fotos.
 * Um comprador tem no máximo uma avaliação por anúncio (pode editar ou excluir).
 */
public class Avaliacao extends BaseDomain {

    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;
    public static final int TAMANHO_TEXTO = 250;
    public static final int MAXIMO_IMAGENS = 3;

    private final UUID anuncioId;
    private final UUID autorId;
    private int nota;
    private String texto;
    private List<String> imagens;

    private Avaliacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                      UUID anuncioId, UUID autorId, int nota, String texto, List<String> imagens) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.anuncioId = anuncioId;
        this.autorId = autorId;
        this.nota = nota;
        this.texto = texto;
        this.imagens = imagens == null ? new ArrayList<>() : new ArrayList<>(imagens);
    }

    public static Avaliacao criar(UUID anuncioId, UUID autorId, int nota, String texto, List<String> imagens) {
        if (anuncioId == null || autorId == null) throw new IllegalArgumentException("Anúncio e autor são obrigatórios");
        Avaliacao a = new Avaliacao(null, LocalDateTime.now(), null, null, anuncioId, autorId, NOTA_MINIMA, null, List.of());
        a.editar(nota, texto, imagens);
        return a;
    }

    public static Avaliacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                      UUID anuncioId, UUID autorId, int nota, String texto, List<String> imagens) {
        return new Avaliacao(id, dataCadastro, dataAlteracao, dataExcluido, anuncioId, autorId, nota, texto, imagens);
    }

    /** Valida e troca nota, texto e imagens. */
    public void editar(int nota, String texto, List<String> imagens) {
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("A nota deve ser de " + NOTA_MINIMA + " a " + NOTA_MAXIMA + " estrelas");
        }
        String t = texto == null || texto.isBlank() ? null : texto.strip();
        if (t != null && t.length() > TAMANHO_TEXTO) {
            throw new IllegalArgumentException("O texto da avaliação deve ter no máximo " + TAMANHO_TEXTO + " caracteres");
        }
        List<String> fotos = imagens == null ? List.of() : imagens.stream().filter(i -> i != null && !i.isBlank()).map(String::strip).toList();
        if (fotos.size() > MAXIMO_IMAGENS) {
            throw new IllegalArgumentException("A avaliação aceita no máximo " + MAXIMO_IMAGENS + " imagens");
        }
        this.nota = nota;
        this.texto = t;
        this.imagens = new ArrayList<>(fotos);
    }

    public boolean pertenceA(UUID usuarioId) {
        return autorId.equals(usuarioId);
    }

    public UUID getAnuncioId() { return anuncioId; }
    public UUID getAutorId() { return autorId; }
    public int getNota() { return nota; }
    public String getTexto() { return texto; }
    public List<String> getImagens() { return List.copyOf(imagens); }
}
