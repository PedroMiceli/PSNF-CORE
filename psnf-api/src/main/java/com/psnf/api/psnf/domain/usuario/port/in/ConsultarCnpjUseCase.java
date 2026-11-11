package com.psnf.api.psnf.domain.usuario.port.in;

import com.psnf.api.psnf.domain.usuario.model.EmpresaConsultada;

public interface ConsultarCnpjUseCase {
    /** dados públicos do CNPJ (para preencher o cadastro); 404 se a base pública não o tem */
    EmpresaConsultada consultar(String cnpj);
}
