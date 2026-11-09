package com.psnf.api.psnf.domain.avaliacao.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * Nota média (1 casa) e quantidade de avaliações de um anúncio, com a distribuição por estrela.
 *
 * @param porNota quantidade de avaliações com 1, 2, 3, 4 e 5 estrelas (índices 0 a 4)
 */
public record ResumoAvaliacoes(BigDecimal media, int total, int[] porNota) {

    public static final ResumoAvaliacoes VAZIO = new ResumoAvaliacoes(null, 0, new int[5]);

    public static ResumoAvaliacoes de(List<Avaliacao> avaliacoes) {
        if (avaliacoes.isEmpty()) return VAZIO;
        int[] por = new int[5];
        avaliacoes.forEach(a -> por[a.getNota() - 1]++);
        return de(avaliacoes.stream().mapToInt(Avaliacao::getNota).sum(), avaliacoes.size(), por);
    }

    public static ResumoAvaliacoes de(long somaNotas, int total, int[] porNota) {
        if (total == 0) return VAZIO;
        return new ResumoAvaliacoes(BigDecimal.valueOf(somaNotas).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP),
                total, porNota == null ? new int[5] : porNota);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ResumoAvaliacoes r && total == r.total && Arrays.equals(porNota, r.porNota)
                && (media == null ? r.media == null : r.media != null && media.compareTo(r.media) == 0);
    }

    @Override
    public int hashCode() {
        return 31 * total + Arrays.hashCode(porNota);
    }

    @Override
    public String toString() {
        return "ResumoAvaliacoes[media=" + media + ", total=" + total + ", porNota=" + Arrays.toString(porNota) + "]";
    }
}
