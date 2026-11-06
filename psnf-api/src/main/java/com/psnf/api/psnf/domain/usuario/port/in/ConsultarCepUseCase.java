package com.psnf.api.psnf.domain.usuario.port.in;

import com.psnf.api.psnf.domain.usuario.model.EnderecoConsultado;

public interface ConsultarCepUseCase {

    /** Aceita o CEP com ou sem máscara ("01310-100" ou "01310100"). */
    EnderecoConsultado consultar(String cep);
}
