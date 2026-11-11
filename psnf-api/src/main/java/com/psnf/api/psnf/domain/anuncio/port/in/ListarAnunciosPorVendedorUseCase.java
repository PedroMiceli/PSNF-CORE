package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.List;
import java.util.UUID;

public interface ListarAnunciosPorVendedorUseCase {

    /** o próprio vendedor vê todos; sem CNPJ, os exclusivos para empresas ficam de fora */
    List<Anuncio> listarPorVendedor(UUID vendedorId, UUID visitanteId);
}
