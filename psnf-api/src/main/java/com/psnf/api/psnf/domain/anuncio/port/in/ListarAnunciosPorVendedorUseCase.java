package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.List;
import java.util.UUID;

public interface ListarAnunciosPorVendedorUseCase {

    List<Anuncio> listarPorVendedor(UUID vendedorId);
}
