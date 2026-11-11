package com.psnf.api.psnf.domain.anuncio.exception;

import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;

/** Anúncio vendido somente para empresas e o comprador não tem CNPJ cadastrado (HTTP 422). */
public class CompradorSemCnpjException extends RegraDeNegocioException {
    public CompradorSemCnpjException(String tituloAnuncio) {
        super("O anúncio '" + tituloAnuncio + "' é vendido somente para empresas: o comprador precisa ter CNPJ cadastrado no perfil");
    }
}
