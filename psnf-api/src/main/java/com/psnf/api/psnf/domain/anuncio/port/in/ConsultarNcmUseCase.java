package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Ncm;

import java.util.List;

public interface ConsultarNcmUseCase {

    /** Busca por parte da descrição ou do código (mínimo de 3 caracteres). Devolve só códigos completos (8 dígitos). */
    List<Ncm> pesquisar(String termo);

    /** Aceita o código com ou sem pontos ("73181500" ou "7318.15.00"). */
    Ncm consultar(String codigo);
}
