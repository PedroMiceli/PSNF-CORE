package com.psnf.api.psnf.domain.usuario.port.out;

import com.psnf.api.psnf.domain.usuario.model.EmpresaConsultada;

import java.util.Optional;

/**
 * Consulta do CNPJ na Receita Federal (implementação atual: BrasilAPI CNPJ v1).
 *
 * @throws com.psnf.api.psnf.domain.usuario.exception.ConsultaCnpjIndisponivelException se o serviço não responder
 */
public interface ConsultaCnpjPort {

    /** @param cnpj normalizado (14 caracteres); vazio se a base pública não tem este CNPJ */
    Optional<EmpresaConsultada> consultar(String cnpj);
}
