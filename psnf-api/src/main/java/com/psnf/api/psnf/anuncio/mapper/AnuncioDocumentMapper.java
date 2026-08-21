package com.psnf.api.psnf.anuncio.mapper;

import com.psnf.api.psnf.anuncio.document.AnuncioDocument;
import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import com.psnf.api.psnf.anuncio.domain.models.ImagemAnuncio;
import com.psnf.api.psnf.anuncio.domain.models.Valor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AnuncioDocumentMapper {

    //TRANSFORMA ANUNCIO PARA SALVAR NO ELASTICSEARCH
    public AnuncioDocument toDocument(Anuncio anuncio) {
        AnuncioDocument doc = new AnuncioDocument();
        doc.setId(anuncio.getId().toString());
        doc.setTitulo(anuncio.getTitulo());
        doc.setDescricao(anuncio.getDescricao());
        doc.setAtivo(anuncio.isAtivo());
        doc.setCriadoEm(anuncio.getDataCadastro()); // supondo que BaseEntity tenha isso

        // pega só a imagem marcada como principal
        anuncio.getImagens().stream()
                .filter(ImagemAnuncio::isPrincipal)
                .findFirst()
                .ifPresent(img -> doc.setImagemPrincipal(img.getCaminho()));

        // pega o menor valor entre as variações/valores, pra exibir "a partir de R$ X"
        anuncio.getValores().stream()
                .map(Valor::getValorUnidade)
                .min(BigDecimal::compareTo)
                .ifPresent(doc::setPrecoMenor);

        doc.setCategoriaId(anuncio.getCategoria().getId().toString());
        doc.setCategoriaNome(anuncio.getCategoria().getNome());

        doc.setVendedorId(anuncio.getVendedor().getId().toString());
        doc.setVendedorNome(anuncio.getVendedor().getNome());

        return doc;
    }
}
