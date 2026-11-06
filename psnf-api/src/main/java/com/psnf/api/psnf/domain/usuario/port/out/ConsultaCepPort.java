package com.psnf.api.psnf.domain.usuario.port.out;

import com.psnf.api.psnf.domain.usuario.model.EnderecoConsultado;

import java.util.Optional;

/**
 * Consulta de endereço por CEP em um serviço externo (implementação atual: BrasilAPI CEP v2).
 *
 * @throws com.psnf.api.psnf.domain.usuario.exception.ConsultaCepIndisponivelException se o serviço não responder
 */
public interface ConsultaCepPort {

    /** @param cep somente os 8 dígitos */
    Optional<EnderecoConsultado> consultar(String cep);
}
