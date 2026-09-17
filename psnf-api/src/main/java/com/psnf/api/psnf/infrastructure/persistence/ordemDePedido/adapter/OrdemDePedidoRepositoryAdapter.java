package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter;

import com.psnf.api.psnf.domain.anuncio.exception.AnuncioNaoEncontradoException;
import com.psnf.api.psnf.domain.anuncio.exception.OrdemDePedidoNaoEncontrada;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ImagemAnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ValorJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.VariacaoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper.OrdemDePedidoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository.OrdemDePedidoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
@Component
@RequiredArgsConstructor
public class OrdemDePedidoRepositoryAdapter implements OrdemDePedidoRepositoryPort {

    private final OrdemDePedidoJpaRepository ordemDePedidoJpaRepository;

    @Override
    public OrdemDePedido save(OrdemDePedido ordemDePedido) {
        return null;
    }

    @Override
    public OrdemDePedido buscarPorId(UUID ordemDePedido) {
        OrdemDePedidoJpa ordemDePedidoJpa = ordemDePedidoJpaRepository.findById(ordemDePedido)
                .orElseThrow(() -> new OrdemDePedidoNaoEncontrada(ordemDePedido));

        return OrdemDePedidoJpaMapper.toDomain(ordemDePedidoJpa);
    }
}
