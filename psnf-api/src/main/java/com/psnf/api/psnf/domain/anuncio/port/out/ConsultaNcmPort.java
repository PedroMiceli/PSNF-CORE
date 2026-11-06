package com.psnf.api.psnf.domain.anuncio.port.out;

import com.psnf.api.psnf.domain.anuncio.model.Ncm;

import java.util.List;
import java.util.Optional;

/**
 * Tabela NCM vinda de um serviço externo.
 *
 * @throws com.psnf.api.psnf.domain.shared.exception.ServicoExternoIndisponivelException se o serviço não responder
 */
public interface ConsultaNcmPort {

    /** Itens cuja descrição ou código contém o termo (inclui capítulos e posições, não só códigos de 8 dígitos). */
    List<Ncm> pesquisar(String termo);

    /** @param codigo os 8 dígitos, sem pontos */
    Optional<Ncm> consultar(String codigo);
}
