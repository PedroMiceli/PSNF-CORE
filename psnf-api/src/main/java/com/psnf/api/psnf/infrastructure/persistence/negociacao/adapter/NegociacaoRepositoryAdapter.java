package com.psnf.api.psnf.infrastructure.persistence.negociacao.adapter;

import com.psnf.api.psnf.domain.negociacao.exception.NegociacaoNaoEncontradaException;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.port.out.NegociacaoRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.adapter.mapper.NegociacaoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.repository.NegociacaoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NegociacaoRepositoryAdapter implements NegociacaoRepositoryPort {

    private final NegociacaoJpaRepository negociacaoJpaRepository;
    private final UsuarioJPARepository usuarioJPARepository;
    private final AnuncioJPARepository anuncioJPARepository;

    @Override
    @Transactional
    public Negociacao save(Negociacao negociacao) {
        var entity = NegociacaoJpaMapper.toEntity(negociacao, usuarioJPARepository::getReferenceById, anuncioJPARepository::getReferenceById);
        return NegociacaoJpaMapper.toDomain(negociacaoJpaRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Negociacao buscarPorId(UUID id) {
        return negociacaoJpaRepository.findById(id).map(NegociacaoJpaMapper::toDomain)
                .orElseThrow(() -> new NegociacaoNaoEncontradaException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Negociacao> buscarPorAnuncioEComprador(UUID anuncioId, UUID compradorId) {
        return negociacaoJpaRepository.findByAnuncioIdAndCompradorId(anuncioId, compradorId).map(NegociacaoJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Negociacao> listarPorParticipante(UUID usuarioId) {
        return negociacaoJpaRepository.listarPorParticipante(usuarioId).stream().map(NegociacaoJpaMapper::toDomain).toList();
    }
}
