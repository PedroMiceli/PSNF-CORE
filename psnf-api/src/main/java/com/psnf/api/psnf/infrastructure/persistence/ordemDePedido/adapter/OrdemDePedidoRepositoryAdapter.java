package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter;

import com.psnf.api.psnf.domain.ordemDePedido.exception.OrdemDePedidoNaoEncontradaException;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.repository.NegociacaoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper.OrdemDePedidoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository.OrdemDePedidoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemDePedidoRepositoryAdapter implements OrdemDePedidoRepositoryPort {

    private final OrdemDePedidoJpaRepository ordemDePedidoJpaRepository;
    private final UsuarioJPARepository usuarioJPARepository;
    private final AnuncioJPARepository anuncioJPARepository;
    private final NegociacaoJpaRepository negociacaoJpaRepository;

    @Override
    public OrdemDePedido save(OrdemDePedido ordemDePedido) {
        OrdemDePedidoJpa entity = OrdemDePedidoJpaMapper.toEntity(
                ordemDePedido,
                usuarioJPARepository::getReferenceById,
                anuncioJPARepository::getReferenceById,
                negociacaoJpaRepository::getReferenceById
        );
        // itens, pagamentos, mensagens e notas fiscais são salvos em cascata
        return OrdemDePedidoJpaMapper.toDomain(ordemDePedidoJpaRepository.saveAndFlush(entity));
    }

    @Override
    public OrdemDePedido buscarPorId(UUID ordemDePedidoId) {
        return ordemDePedidoJpaRepository.findById(ordemDePedidoId)
                .map(OrdemDePedidoJpaMapper::toDomain)
                .orElseThrow(() -> new OrdemDePedidoNaoEncontradaException(ordemDePedidoId));
    }

    @Override
    public List<OrdemDePedido> buscarPorComprador(UUID compradorId) {
        return ordemDePedidoJpaRepository.findByCompradorIdOrderByDataCadastroDesc(compradorId).stream()
                .map(OrdemDePedidoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<UUID> idsComMensagensNaoLidas(UUID usuarioId) {
        return ordemDePedidoJpaRepository.idsComMensagensNaoLidas(usuarioId);
    }

    @Override
    public boolean existeCompraEntregue(UUID compradorId, UUID anuncioId) {
        return ordemDePedidoJpaRepository.existeCompraEntregue(compradorId, anuncioId);
    }

    @Override
    public List<OrdemDePedido> buscarPorCompradorEVendedor(UUID compradorId, UUID vendedorId) {
        return ordemDePedidoJpaRepository.findByCompradorIdAndVendedorIdOrderByDataCadastroDesc(compradorId, vendedorId).stream()
                .map(OrdemDePedidoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<OrdemDePedido> buscarPorVendedor(UUID vendedorId) {
        return ordemDePedidoJpaRepository.findByVendedorIdOrderByDataCadastroDesc(vendedorId).stream()
                .map(OrdemDePedidoJpaMapper::toDomain)
                .toList();
    }
}
