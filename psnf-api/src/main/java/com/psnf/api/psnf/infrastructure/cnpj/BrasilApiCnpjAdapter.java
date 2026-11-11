package com.psnf.api.psnf.infrastructure.cnpj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.psnf.api.psnf.domain.usuario.exception.ConsultaCnpjIndisponivelException;
import com.psnf.api.psnf.domain.usuario.model.EmpresaConsultada;
import com.psnf.api.psnf.domain.usuario.port.out.ConsultaCnpjPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

/**
 * Consulta de CNPJ na BrasilAPI (dados abertos da Receita Federal): https://brasilapi.com.br/docs#tag/CNPJ
 * Serviço público e gratuito: as consultas do front passam pelo rate limit da API ("consultas-externas").
 */
@Component
public class BrasilApiCnpjAdapter implements ConsultaCnpjPort {

    private static final Logger logger = LoggerFactory.getLogger(BrasilApiCnpjAdapter.class);

    /** Formato da resposta da BrasilAPI (apenas os campos usados). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaBrasilApi(String cnpj,
                             @JsonProperty("razao_social") String razaoSocial,
                             @JsonProperty("nome_fantasia") String nomeFantasia,
                             @JsonProperty("descricao_situacao_cadastral") String situacao,
                             String cep, String logradouro, String numero, String complemento, String bairro,
                             String municipio, String uf) {
    }

    private final RestClient restClient;

    public BrasilApiCnpjAdapter(@Value("${psnf.cnpj.brasilapi-url:https://brasilapi.com.br/api/cnpj/v1}") String url,
                                @Value("${psnf.cnpj.timeout:8s}") Duration timeout) {
        SimpleClientHttpRequestFactory http = new SimpleClientHttpRequestFactory();
        http.setConnectTimeout(timeout);
        http.setReadTimeout(timeout);
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(http)
                // a BrasilAPI recusa alguns clientes genéricos: a API se identifica
                .defaultHeader(HttpHeaders.USER_AGENT, "PSNF/1.0 (marketplace B2B)")
                .build();
    }

    @Override
    public Optional<EmpresaConsultada> consultar(String cnpj) {
        try {
            RespostaBrasilApi r = restClient.get().uri("/{cnpj}", cnpj).retrieve().body(RespostaBrasilApi.class);
            if (r == null) return Optional.empty();
            return Optional.of(new EmpresaConsultada(cnpj, limpar(r.razaoSocial()), limpar(r.nomeFantasia()),
                    limpar(r.situacao()), formatarCep(r.cep()), limpar(r.logradouro()), limpar(r.numero()),
                    limpar(r.complemento()), limpar(r.bairro()), limpar(r.municipio()), limpar(r.uf())));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return Optional.empty();
            }
            throw indisponivel(cnpj, e);
        } catch (RestClientException e) {
            throw indisponivel(cnpj, e);
        }
    }

    private ConsultaCnpjIndisponivelException indisponivel(String cnpj, Exception e) {
        logger.warn("Falha ao consultar o CNPJ {} na BrasilAPI: {}", cnpj, e.getMessage());
        return new ConsultaCnpjIndisponivelException("Não foi possível consultar o CNPJ na Receita agora. Tente de novo em instantes.", e);
    }

    private static String formatarCep(String cep) {
        String d = cep == null ? "" : cep.replaceAll("\\D", "");
        return d.length() == 8 ? d.substring(0, 5) + "-" + d.substring(5) : null;
    }

    private static String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
