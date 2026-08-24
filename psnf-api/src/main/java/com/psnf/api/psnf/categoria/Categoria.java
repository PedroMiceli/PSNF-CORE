package com.psnf.api.psnf.categoria;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.models.BaseEntity;
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
public class Categoria extends BaseEntity {

    private String nome;
    
    public Categoria(String nome) {
        this.nome = nome;
    }

}
