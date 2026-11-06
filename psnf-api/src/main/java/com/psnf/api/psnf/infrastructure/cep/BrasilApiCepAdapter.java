package com.psnf.api.psnf.infrastructure.cep;

import com.psnf.api.psnf.domain.usuario.exception.ConsultaCepIndisponivelException;
import com.psnf.api.psnf.domain.usuario.model.EnderecoConsultado;
import com.psnf.api.psnf.domain.usuario.port.out.ConsultaCepPort;
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
import java.util.Optional;

/**
 * Consulta de CEP na BrasilAPI (CEP v2): https://brasilapi.com.br/docs#tag/CEP-V2
 * A própria BrasilAPI consulta vários provedores (Correios, ViaCEP, WideNet...) e devolve o primeiro que responder.
 */
@Component
public class BrasilApiCepAdapter implements ConsultaCepPort {

    private static final Logger logger = LoggerFactory.getLogger(BrasilApiCepAdapter.class);

    /** Formato da resposta da BrasilAPI (apenas os campos usados). */
    record RespostaBrasilApi(String cep, String state, String city, String neighborhood, String street) {
    }

    private final RestClient restClient;

    public BrasilApiCepAdapter(@Value("${psnf.cep.brasilapi-url:https://brasilapi.com.br/api/cep/v2}") String url,
                               @Value("${psnf.cep.timeout:5s}") Duration timeout) {
        SimpleClientHttpRequestFactory http = new SimpleClientHttpRequestFactory();
        http.setConnectTimeout(timeout);
        http.setReadTimeout(timeout);
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(http).build();
    }

    @Override
    public Optional<EnderecoConsultado> consultar(String cep) {
        try {
            RespostaBrasilApi r = restClient.get().uri("/{cep}", cep).retrieve().body(RespostaBrasilApi.class);
            if (r == null) return Optional.empty();
            return Optional.of(new EnderecoConsultado(
                    formatar(r.cep() != null ? r.cep() : cep),
                    vazioParaNull(r.street()),
                    vazioParaNull(r.neighborhood()),
                    vazioParaNull(r.city()),
                    vazioParaNull(r.state())
            ));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return Optional.empty();
            }
            throw indisponivel(cep, e);
        } catch (RestClientException e) {
            throw indisponivel(cep, e);
        }
    }

    private ConsultaCepIndisponivelException indisponivel(String cep, Exception e) {
        logger.warn("Falha ao consultar o CEP {} na BrasilAPI: {}", cep, e.getMessage());
        return new ConsultaCepIndisponivelException("Não foi possível consultar o CEP agora. Preencha o endereço manualmente.", e);
    }

    private static String formatar(String cep) {
        String d = cep.replaceAll("\\D", "");
        return d.length() == 8 ? d.substring(0, 5) + "-" + d.substring(5) : cep;
    }

    private static String vazioParaNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
