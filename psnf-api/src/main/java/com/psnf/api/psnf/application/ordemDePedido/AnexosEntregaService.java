package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.AnexosEntregaUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnexosEntregaService implements AnexosEntregaUseCase {

    private final OrdemDePedidoRepositoryPort ordens;

    @Override
    @Transactional
    public OrdemDePedido adicionar(UUID ordemId, UUID usuarioId, String caminho) {
        OrdemDePedido ordem = ordens.buscarPorId(ordemId);
        ordem.adicionarAnexoEntrega(usuarioId, caminho);
        return ordens.save(ordem);
    }

    @Override
    @Transactional
    public OrdemDePedido remover(UUID ordemId, UUID usuarioId, UUID anexoId) {
        OrdemDePedido ordem = ordens.buscarPorId(ordemId);
        ordem.removerAnexoEntrega(usuarioId, anexoId);
        return ordens.save(ordem);
    }
}
