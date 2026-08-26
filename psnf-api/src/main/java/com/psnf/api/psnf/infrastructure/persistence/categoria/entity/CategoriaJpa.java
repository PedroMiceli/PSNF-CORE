package com.psnf.api.psnf.infrastructure.persistence.categoria.entity;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class CategoriaJpa extends BaseEntity {

    private String nome;
    
    public CategoriaJpa(String nome) {
        this.nome = nome;
    }

}
