package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter;

import com.psnf.api.psnf.domain.anuncio.exception.AnuncioNaoEncontradoException;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.ImagemJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.ValorJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.VariacaoJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ImagemAnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.ValorJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.VariacaoJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.ImagemAnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.ValorAnuncioJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.VariacaoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnuncioRepositoryAdapter implements AnuncioRepositoryPort {

    private final AnuncioJPARepository anuncioJPARepository;
    private final ImagemAnuncioJPARepository imagemAnuncioJPARepository;
    private final ValorAnuncioJpaRepository  valorAnuncioJpaRepository;
    private final VariacaoJpaRepository variacaoJpaRepository;


    @Override
    public Anuncio save(Anuncio anuncio) {
        AnuncioJpa anuncioJpa = anuncioJPARepository.saveAndFlush(AnuncioJpaMapper.toEntity(anuncio));

        List<ValorJpa> valoresJpa = anuncio.getValores().stream()
                .map(valor -> ValorJpaMapper.toEntity(valor, anuncioJpa))
                .toList();
        valorAnuncioJpaRepository.saveAll(valoresJpa);

        List<ImagemAnuncioJpa> imagensJpa = anuncio.getImagens().stream()
                .map(imagem -> ImagemJpaMapper.toEntity(imagem, anuncioJpa.getId()))
                .toList();
        imagemAnuncioJPARepository.saveAll(imagensJpa);

        List<VariacaoJpa> variacoesJpa = anuncio.getVariacoes().stream()
                .map(variacao -> VariacaoJpaMapper.toEntity(variacao, anuncioJpa))
                .toList();
        variacaoJpaRepository.saveAll(variacoesJpa);

        return AnuncioJpaMapper.toDomain(anuncioJpa, valoresJpa, imagensJpa, variacoesJpa);
    }

    @Override
    public Anuncio buscarPorId(UUID id) {
        AnuncioJpa anuncioJpa = anuncioJPARepository.findById(id)
                .orElseThrow(() -> new AnuncioNaoEncontradoException(id));

        List<ValorJpa> valores = valorAnuncioJpaRepository.buscarPorAnuncioId(id);

        List<ImagemAnuncioJpa> imagens = imagemAnuncioJPARepository.buscarPorAnuncioId(id);

        List<VariacaoJpa> variacoes = variacaoJpaRepository.buscarPorAnuncioId(id);

        return AnuncioJpaMapper.toDomain(anuncioJpa, valores, imagens, variacoes);
    }

}
