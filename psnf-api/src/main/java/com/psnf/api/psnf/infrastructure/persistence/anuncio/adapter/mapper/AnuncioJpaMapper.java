package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.Caracteristica;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.CaracteristicaJpa;

import java.util.List;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.categoria.entity.CategoriaJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

public class AnuncioJpaMapper {

    //transforma o domain para a entidade de infra (categoria e vendedor chegam como referência)
    public static AnuncioJpa toEntity(Anuncio anuncio, CategoriaJpa categoria, UsuarioJpa vendedor) {
        AnuncioJpa jpa = new AnuncioJpa();
        jpa.restaurarBase(anuncio.getId(), anuncio.getDataCadastro(), anuncio.getDataAlteracao(), anuncio.getDataExcluido());
        jpa.setTitulo(anuncio.getTitulo());
        jpa.setDescricao(anuncio.getDescricao());
        jpa.setAtivo(anuncio.isAtivo());
        jpa.setExigeCnpj(anuncio.isExigeCnpj());
        jpa.setCategoria(categoria);
        jpa.setVendedor(vendedor);
        anuncio.getValores().forEach(v -> jpa.getValores().add(ValorJpaMapper.toEntity(v, jpa)));
        anuncio.getImagens().forEach(i -> jpa.getImagens().add(ImagemJpaMapper.toEntity(i, jpa)));
        anuncio.getVariacoes().forEach(v -> jpa.getVariacoes().add(VariacaoJpaMapper.toEntity(v, jpa)));
        jpa.setInformacoes(InformacoesProdutoJpaMapper.toEntity(anuncio.getInformacoes()));
        List<Caracteristica> ficha = anuncio.getCaracteristicas();
        for (int i = 0; i < ficha.size(); i++) {
            CaracteristicaJpa c = new CaracteristicaJpa();
            c.setNome(ficha.get(i).nome());
            c.setValor(ficha.get(i).valor());
            c.setPosicao(i);
            c.setAnuncio(jpa);
            jpa.getCaracteristicas().add(c);
        }
        return jpa;
    }

    //transforma o jpa para domain
    public static Anuncio toDomain(AnuncioJpa jpa) {
        Anuncio anuncio = Anuncio.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getTitulo(),
                jpa.getDescricao(),
                jpa.isAtivo(),
                jpa.getCategoria().getId(),
                jpa.getVendedor().getId(),
                jpa.getValores().stream().map(ValorJpaMapper::toDomain).toList(),
                jpa.getImagens().stream().map(ImagemJpaMapper::toDomain).toList(),
                jpa.getVariacoes().stream().map(VariacaoJpaMapper::toDomain).toList(),
                InformacoesProdutoJpaMapper.toDomain(jpa.getInformacoes()),
                jpa.getCaracteristicas().stream().map(c -> new Caracteristica(c.getNome(), c.getValor())).toList()
        );
        anuncio.definirExigeCnpj(jpa.isExigeCnpj());
        return anuncio;
    }
}
