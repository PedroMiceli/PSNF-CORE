package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.ListarAnunciosPorVendedorUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListarAnunciosPorVendedorService implements ListarAnunciosPorVendedorUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Anuncio> listarPorVendedor(UUID vendedorId) {
        return anuncioRepositoryPort.buscarPorVendedor(vendedorId);
    }
}
