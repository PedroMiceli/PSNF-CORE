package com.psnf.api.psnf.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Categoria extends BaseEntity{

    private String nome;

    @OneToMany(mappedBy = "categoria")
    private List<Anuncio> anuncios = new ArrayList<>();


    public Categoria(String nome) {
        this.nome = nome;
    }

}
