package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.AnuncioResumo;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.elastic.AnuncioElasticDocument;

import java.math.BigDecimal;
import java.util.UUID;

public class AnuncioElasticMapper {

    public static AnuncioElasticDocument toElasticDocument(Anuncio anuncio, String categoriaNome, String vendedorNome) {
        AnuncioElasticDocument doc = new AnuncioElasticDocument();
        doc.setId(anuncio.getId().toString());
        doc.setTitulo(anuncio.getTitulo());
        doc.setDescricao(anuncio.getDescricao());
        doc.setAtivo(anuncio.isAtivo());
        doc.setCriadoEm(anuncio.getDataCadastro());

        // pega só a imagem marcada como principal
        doc.setImagemPrincipal(anuncio.getImagemPrincipal().map(ImagemAnuncio::getCaminho).orElse(null));

        // pega o menor valor entre as faixas, pra exibir "a partir de R$ X"
        BigDecimal menor = anuncio.getMenorValor();
        doc.setPrecoMenor(menor == null ? null : menor.doubleValue());

        //id da categoria e o nome dela
        doc.setCategoriaId(anuncio.getCategoria().toString());
        doc.setCategoriaNome(categoriaNome);

        //id do vendedor e o nome dele
        doc.setVendedorId(anuncio.getVendedor().toString());
        doc.setVendedorNome(vendedorNome);

        return doc;
    }

    public static AnuncioResumo toResumo(AnuncioElasticDocument doc) {
        return new AnuncioResumo(
                UUID.fromString(doc.getId()),
                doc.getTitulo(),
                doc.getDescricao(),
                doc.getImagemPrincipal(),
                doc.getPrecoMenor() == null ? null : BigDecimal.valueOf(doc.getPrecoMenor()),
                doc.getCategoriaId() == null ? null : UUID.fromString(doc.getCategoriaId()),
                doc.getCategoriaNome(),
                doc.getVendedorId() == null ? null : UUID.fromString(doc.getVendedorId()),
                doc.getVendedorNome()
        );
    }
}
