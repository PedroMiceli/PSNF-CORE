package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.elastic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "anuncios", createIndex = false)
@Setter
@Getter
@NoArgsConstructor
public class AnuncioElasticDocument {

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
    private Double precoMenor;

    @Field(type = FieldType.Keyword)
    private String categoriaId;

    @Field(type = FieldType.Text)
    private String categoriaNome;

    @Field(type = FieldType.Keyword)
    private String vendedorId;

    @Field(type = FieldType.Text)
    private String vendedorNome;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime criadoEm;
}
