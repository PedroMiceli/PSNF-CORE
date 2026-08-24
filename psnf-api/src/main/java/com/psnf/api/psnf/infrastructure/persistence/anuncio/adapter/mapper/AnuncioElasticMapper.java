package com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.elastic.AnuncioElasticDocument;

import java.math.BigDecimal;


public class AnuncioElasticMapper {

    public static AnuncioElasticDocument toElasticDocument(Anuncio anuncio, String imagemPrincipal, String categoriaNome, BigDecimal precoMenor, String vendedorNome) {
        AnuncioElasticDocument doc = new AnuncioElasticDocument();
        doc.setId(anuncio.getId().toString());
        doc.setTitulo(anuncio.getTitulo());
        doc.setDescricao(anuncio.getDescricao());
        doc.setAtivo(anuncio.isAtivo());
        doc.setCriadoEm(anuncio.getDataCadastro());

        // pega só a imagem marcada como principal
        doc.setImagemPrincipal(imagemPrincipal);

        // pega o menor valor entre as variações/valores, pra exibir "a partir de R$ X"
        doc.setPrecoMenor(precoMenor);

        //id da categoria e o nome dela
        doc.setCategoriaId(anuncio.getCategoria().toString());
        doc.setCategoriaNome(categoriaNome);

        //id do vendedor e o nome dele
        doc.setVendedorId(anuncio.getVendedor().toString());
        doc.setVendedorNome(vendedorNome);

        return doc;
    }

}
