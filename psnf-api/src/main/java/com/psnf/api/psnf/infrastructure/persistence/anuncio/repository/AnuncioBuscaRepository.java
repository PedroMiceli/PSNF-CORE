package com.psnf.api.psnf.infrastructure.persistence.anuncio.repository;

import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.domain.shared.model.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Busca de anúncios com o full-text search do PostgreSQL (coluna "busca" + pg_trgm).
 * A estrutura (coluna gerada, índices, extensões) está nas migrações (db/migration/V1__esquema_inicial.sql).
 *
 * Um anúncio é encontrado quando:
 *  - as palavras digitadas (ou o começo delas) aparecem no título/descrição, ignorando acentos e
 *    plural/singular (to_tsquery com prefixo: "carri" -> "carri:*"); ou
 *  - o termo é parecido com alguma parte do título (pg_trgm, tolera erros de digitação).
 * Ordena por relevância (palavra inteira > começo de palavra; título > descrição; semelhança do título)
 * e, em seguida, pelos mais recentes.
 */
@Repository
@RequiredArgsConstructor
public class AnuncioBuscaRepository {

    private static final String TSQUERY = "to_tsquery('portuguese', psnf_unaccent(:tsquery))";
    /** mesmas palavras, sem prefixo: só palavras inteiras (usado para dar mais peso na ordenação) */
    private static final String TSQUERY_EXATA = "to_tsquery('portuguese', psnf_unaccent(:tsqueryExata))";
    private static final String TERMO = "psnf_unaccent(lower(:termo))";
    private static final String TITULO = "psnf_unaccent(lower(a.titulo))";

    private final NamedParameterJdbcTemplate jdbc;

    /** Devolve os ids da página pedida, já na ordem do resultado, e o total de anúncios encontrados. */
    public Pagina<UUID> pesquisarIds(PesquisarAnunciosQuery query) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tamanho", query.tamanho())
                .addValue("offset", (long) query.pagina() * query.tamanho());

        StringBuilder where = new StringBuilder(" WHERE a.ativo");
        if (query.somenteSemExigenciaDeCnpj()) {
            // sem CNPJ: só anúncios liberados para pessoa física (e os próprios)
            where.append(" AND (NOT a.exige_cnpj OR a.vendedor_id = :visitante)");
            params.addValue("visitante", query.visitanteId());
        }
        if (query.categoriaId() != null) {
            where.append(" AND a.categoria_id = :categoriaId");
            params.addValue("categoriaId", query.categoriaId());
        }

        List<String> palavras = query.temTermo() ? palavras(query.termo()) : List.of();
        String ordem = " ORDER BY a.data_cadastro DESC, a.id";
        if (!palavras.isEmpty()) {
            String termoLimpo = String.join(" ", palavras);
            // tolerância a erro de digitação só faz sentido com termos de 3+ caracteres
            boolean usarSemelhanca = termoLimpo.length() >= 3;
            where.append(" AND (a.busca @@ ").append(TSQUERY);
            if (usarSemelhanca) where.append(" OR ").append(TERMO).append(" <% ").append(TITULO);
            where.append(")");
            // 1º quem contém as palavras inteiras ("cabo"), à frente de quem só casa pelo começo
            //    ("cabelo" também começa com "cab") ou por semelhança;
            // 2º dentro de cada grupo: relevância do texto (título pesa mais) + semelhança do título.
            ordem = " ORDER BY (a.busca @@ " + TSQUERY_EXATA + ") DESC,"
                    + " ts_rank_cd(a.busca, " + TSQUERY_EXATA + ") + ts_rank_cd(a.busca, " + TSQUERY + ")"
                    + (usarSemelhanca ? " + 0.5 * word_similarity(" + TERMO + ", " + TITULO + ")" : "")
                    + " DESC, a.data_cadastro DESC, a.id";
            params.addValue("tsquery", montarTsquery(palavras))
                  .addValue("tsqueryExata", String.join(" & ", palavras))
                  .addValue("termo", termoLimpo);
        }

        Long total = jdbc.queryForObject("SELECT count(*) FROM anuncio a" + where, params, Long.class);
        List<UUID> ids = total == null || total == 0
                ? List.of()
                : jdbc.queryForList("SELECT a.id FROM anuncio a" + where + ordem + " LIMIT :tamanho OFFSET :offset",
                        params, UUID.class);

        return new Pagina<>(ids, query.pagina(), query.tamanho(), total == null ? 0 : total);
    }

    /**
     * Palavras que entram na busca: só letras e números (caracteres especiais do usuário nunca chegam
     * ao tsquery). Letras soltas são ignoradas ("c" casaria com qualquer palavra começando com c);
     * números de um dígito são mantidos ("nº 9").
     */
    static List<String> palavras(String termo) {
        return Arrays.stream(termo.toLowerCase().split("[^\\p{L}\\p{N}]+"))
                .filter(p -> p.length() >= 2 || (p.length() == 1 && Character.isDigit(p.charAt(0))))
                .toList();
    }

    /** ["carrinho", "metal"] -> "carrinho:* & metal:*" (busca também pelo começo da palavra). */
    static String montarTsquery(List<String> palavras) {
        return palavras.stream().map(p -> p + ":*").collect(Collectors.joining(" & "));
    }
}
