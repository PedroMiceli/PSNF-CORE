package com.psnf.api.psnf.anuncio.document;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "anuncios")
@Setter
@Getter
@NoArgsConstructor
public class AnuncioDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String titulo;

    @Field(type = FieldType.Text)
    private String descricao;

    @Field(type = FieldType.Boolean)
    private boolean ativo;

    @Field(type = FieldType.Keyword)
    private String imagemPrincipal;

    @Field(type = FieldType.Double)
    private BigDecimal precoMenor;

    @Field(type = FieldType.Keyword)
    private String categoriaId;

    @Field(type = FieldType.Text)
    private String categoriaNome;

    @Field(type = FieldType.Keyword)
    private String vendedorId;

    @Field(type = FieldType.Text)
    private String vendedorNome;

    @Field(type = FieldType.Date)
    private LocalDateTime criadoEm;
}
