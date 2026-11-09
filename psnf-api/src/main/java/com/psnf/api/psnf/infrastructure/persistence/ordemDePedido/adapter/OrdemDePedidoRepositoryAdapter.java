package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter;

import com.psnf.api.psnf.domain.ordemDePedido.exception.OrdemDePedidoNaoEncontradaException;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.ResumoOrdem;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.repository.NegociacaoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper.OrdemDePedidoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.repository.OrdemDePedidoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<ResumoOrdem> resumosEntre(UUID compradorId, UUID vendedorId) {
        return ordemDePedidoJpaRepository.resumosEntre(compradorId, vendedorId).stream()
                .map(r -> new ResumoOrdem((UUID) r[0], (String) r[1], (EnumStatusOrdemDePedido) r[2], (LocalDateTime) r[3],
                        (UUID) r[4], (BigDecimal) r[5], (BigDecimal) r[6]))
                .toList();
    }

    @Override
    public Map<UUID, Long> contarPorNegociacao(Collection<UUID> negociacaoIds) {
        if (negociacaoIds.isEmpty()) return Map.of();
        Map<UUID, Long> mapa = new HashMap<>();
        ordemDePedidoJpaRepository.contarPorNegociacao(negociacaoIds).forEach(r -> mapa.put((UUID) r[0], ((Number) r[1]).longValue()));
        return mapa;
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
    public List<OrdemDePedido> buscarPorVendedor(UUID vendedorId) {
        return ordemDePedidoJpaRepository.findByVendedorIdOrderByDataCadastroDesc(vendedorId).stream()
                .map(OrdemDePedidoJpaMapper::toDomain)
                .toList();
    }
}
