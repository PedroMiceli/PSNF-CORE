package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.domain.models.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AnuncioJpa extends BaseEntity{

    private String titulo;
    private String descricao;
    private boolean ativo;
    private UUID categoria;
    private UUID vendedor;

    public AnuncioJpa(UUID id, String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor) {
        this.setId(id);
        this.titulo = titulo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.categoria = categoria;
        this.vendedor = vendedor;
    }
}
