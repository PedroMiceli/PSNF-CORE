package com.psnf.api.psnf.infrastructure.ncm;

import com.psnf.api.psnf.domain.anuncio.model.Ncm;
import com.psnf.api.psnf.domain.anuncio.port.out.ConsultaNcmPort;
import com.psnf.api.psnf.domain.shared.exception.ServicoExternoIndisponivelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Consulta da tabela NCM na BrasilAPI: https://brasilapi.com.br/docs#tag/NCM
 * A tabela muda raramente, então as respostas ficam num cache em memória (LRU) para não repetir chamadas.
 */
@Component
public class BrasilApiNcmAdapter implements ConsultaNcmPort {

    private static final Logger logger = LoggerFactory.getLogger(BrasilApiNcmAdapter.class);
    private static final int TAMANHO_CACHE = 500;

    /** Formato da resposta da BrasilAPI (apenas os campos usados). */
    record RespostaBrasilApi(String codigo, String descricao) {
    }

    private final RestClient restClient;
    private final Map<String, List<Ncm>> cacheBusca = lru();
    private final Map<String, Optional<Ncm>> cacheCodigo = lru();

    public BrasilApiNcmAdapter(@Value("${psnf.ncm.brasilapi-url:https://brasilapi.com.br/api/ncm/v1}") String url,
                               @Value("${psnf.ncm.timeout:8s}") Duration timeout) {
        SimpleClientHttpRequestFactory http = new SimpleClientHttpRequestFactory();
        http.setConnectTimeout(timeout);
        http.setReadTimeout(timeout);
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(http).build();
    }

    @Override
    public List<Ncm> pesquisar(String termo) {
        String chave = termo.toLowerCase();
        List<Ncm> emCache = cacheBusca.get(chave);
        if (emCache != null) return emCache;
        try {
            RespostaBrasilApi[] r = restClient.get().uri(b -> b.queryParam("search", termo).build())
                    .retrieve().body(RespostaBrasilApi[].class);
            List<Ncm> lista = r == null ? List.of() : Arrays.stream(r).map(BrasilApiNcmAdapter::toNcm).toList();
            cacheBusca.put(chave, lista);
            return lista;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) return List.of();
            throw indisponivel(termo, e);
        } catch (RestClientException e) {
            throw indisponivel(termo, e);
        }
    }

    @Override
    public Optional<Ncm> consultar(String codigo) {
        Optional<Ncm> emCache = cacheCodigo.get(codigo);
        if (emCache != null) return emCache;
        try {
            RespostaBrasilApi r = restClient.get().uri("/{codigo}", codigo).retrieve().body(RespostaBrasilApi.class);
            Optional<Ncm> ncm = Optional.ofNullable(r).map(BrasilApiNcmAdapter::toNcm);
            cacheCodigo.put(codigo, ncm);
            return ncm;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                cacheCodigo.put(codigo, Optional.empty());
                return Optional.empty();
            }
            throw indisponivel(codigo, e);
        } catch (RestClientException e) {
            throw indisponivel(codigo, e);
        }
    }

    /** A BrasilAPI devolve a descrição com os traços da hierarquia da tabela ("-- Outros parafusos..."). */
    private static Ncm toNcm(RespostaBrasilApi r) {
        String descricao = r.descricao() == null ? "" : r.descricao().replaceFirst("^[\\s\\-–]+", "").trim();
        return new Ncm(r.codigo(), descricao);
    }

    private ServicoExternoIndisponivelException indisponivel(String termo, Exception e) {
        logger.warn("Falha ao consultar o NCM '{}' na BrasilAPI: {}", termo, e.getMessage());
        return new ServicoExternoIndisponivelException("Não foi possível consultar a tabela NCM agora. Informe o código manualmente.", e);
    }

    private static <V> Map<String, V> lru() {
        return Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, V> mais) {
                return size() > TAMANHO_CACHE;
            }
        });
    }
}
