package com.psnf.api.psnf.infrastructure.persistence.avaliacao.adapter;

import com.psnf.api.psnf.domain.avaliacao.model.Avaliacao;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;
import com.psnf.api.psnf.domain.avaliacao.port.out.AvaliacaoRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.avaliacao.entity.AvaliacaoJpa;
import com.psnf.api.psnf.infrastructure.persistence.avaliacao.repository.AvaliacaoJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AvaliacaoRepositoryAdapter implements AvaliacaoRepositoryPort {

    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final AnuncioJPARepository anuncioJPARepository;
    private final UsuarioJPARepository usuarioJPARepository;

    @Override
    @Transactional
    public Avaliacao save(Avaliacao a) {
        AvaliacaoJpa jpa = new AvaliacaoJpa();
        jpa.restaurarBase(a.getId(), a.getDataCadastro(), a.getDataAlteracao(), a.getDataExcluido());
        jpa.setAnuncio(anuncioJPARepository.getReferenceById(a.getAnuncioId()));
        jpa.setAutor(usuarioJPARepository.getReferenceById(a.getAutorId()));
        jpa.setNota(a.getNota());
        jpa.setTexto(a.getTexto());
        jpa.setImagens(new ArrayList<>(a.getImagens()));
        return toDomain(avaliacaoJpaRepository.saveAndFlush(jpa));
    }

    @Override
    @Transactional
    public void remover(UUID avaliacaoId) {
        avaliacaoJpaRepository.deleteById(avaliacaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Avaliacao> buscarPorAnuncioEAutor(UUID anuncioId, UUID autorId) {
        return avaliacaoJpaRepository.findByAnuncioIdAndAutorId(anuncioId, autorId).map(AvaliacaoRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorAnuncio(UUID anuncioId) {
        return avaliacaoJpaRepository.findByAnuncioIdOrderByDataCadastroDesc(anuncioId).stream()
                .map(AvaliacaoRepositoryAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, ResumoAvaliacoes> resumoPorAnuncios(Collection<UUID> anuncioIds) {
        Map<UUID, ResumoAvaliacoes> mapa = new HashMap<>();
        for (Object[] linha : avaliacaoJpaRepository.resumoPorAnuncios(anuncioIds)) {
            mapa.put((UUID) linha[0], ResumoAvaliacoes.de(((Number) linha[1]).longValue(), ((Number) linha[2]).intValue(), null));
        }
        return mapa;
    }

    private static Avaliacao toDomain(AvaliacaoJpa j) {
        return Avaliacao.restaurar(j.getId(), j.getDataCadastro(), j.getDataAlteracao(), j.getDataExcluido(),
                j.getAnuncio().getId(), j.getAutor().getId(), j.getNota(), j.getTexto(), List.copyOf(j.getImagens()));
    }
}
