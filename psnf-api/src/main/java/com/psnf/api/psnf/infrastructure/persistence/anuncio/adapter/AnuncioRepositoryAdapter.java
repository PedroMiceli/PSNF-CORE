package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter;

import com.psnf.api.psnf.domain.anuncio.exception.AnuncioNaoEncontradoException;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.CategoriaComAnuncios;
import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.shared.model.Pagina;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioBuscaRepository;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.repository.AnuncioJPARepository;
import com.psnf.api.psnf.infrastructure.persistence.categoria.repository.CategoriaJpaRepository;
import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnuncioRepositoryAdapter implements AnuncioRepositoryPort {

    private final AnuncioJPARepository anuncioJPARepository;
    private final CategoriaJpaRepository categoriaJpaRepository;
    private final UsuarioJPARepository usuarioJPARepository;
    private final AnuncioBuscaRepository anuncioBuscaRepository;

    @Override
    public Anuncio save(Anuncio anuncio) {
        AnuncioJpa entity = AnuncioJpaMapper.toEntity(
                anuncio,
                categoriaJpaRepository.getReferenceById(anuncio.getCategoria()),
                usuarioJPARepository.getReferenceById(anuncio.getVendedor())
        );
        // valores, imagens e variações são salvos em cascata
        return AnuncioJpaMapper.toDomain(anuncioJPARepository.saveAndFlush(entity));
    }

    @Override
    public Map<UUID, Anuncio> buscarPorIds(Collection<UUID> ids) {
        if (ids.isEmpty()) return Map.of();
        return anuncioJPARepository.findAllById(ids).stream().map(AnuncioJpaMapper::toDomain)
                .collect(Collectors.toMap(Anuncio::getId, a -> a));
    }

    @Override
    public Anuncio buscarPorId(UUID id) {
        return anuncioJPARepository.findById(id)
                .map(AnuncioJpaMapper::toDomain)
                .orElseThrow(() -> new AnuncioNaoEncontradoException(id));
    }

    @Override
    public List<Anuncio> buscarPorVendedor(UUID vendedorId) {
        return anuncioJPARepository.findByVendedorIdOrderByDataCadastroDesc(vendedorId).stream()
                .map(AnuncioJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<CategoriaComAnuncios> categoriasDoVendedor(UUID vendedorId) {
        return anuncioBuscaRepository.categoriasDoVendedor(vendedorId);
    }

    @Override
    public Pagina<Anuncio> pesquisar(PesquisarAnunciosQuery query) {
        Pagina<UUID> ids = anuncioBuscaRepository.pesquisarIds(query);
        // carrega só os anúncios da página e devolve na ordem de relevância calculada pelo banco
        Map<UUID, AnuncioJpa> porId = anuncioJPARepository.findAllById(ids.itens()).stream()
                .collect(Collectors.toMap(AnuncioJpa::getId, Function.identity()));
        return new Pagina<>(
                ids.itens().stream().map(porId::get).filter(Objects::nonNull).map(AnuncioJpaMapper::toDomain).toList(),
                ids.pagina(), ids.tamanho(), ids.total());
    }
}
