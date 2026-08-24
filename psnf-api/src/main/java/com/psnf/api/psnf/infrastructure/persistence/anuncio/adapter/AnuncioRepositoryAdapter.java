package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnuncioRepositoryAdapter implements AnuncioRepositoryPort {

    private final AnuncioJPARepository anuncioJPARepository;


    @Override
    public Anuncio save(Anuncio anuncio) {
        return AnuncioJpaMapper.toDomain(anuncioJPARepository.saveAndFlush(AnuncioJpaMapper.toEntity(anuncio)));
    }

    @Override
    public Anuncio buscarPorId(UUID anuncio) {
        return null;
    }

}
