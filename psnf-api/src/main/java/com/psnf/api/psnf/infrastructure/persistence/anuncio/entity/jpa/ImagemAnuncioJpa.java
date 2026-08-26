package com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa;

import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class ImagemAnuncioJpa extends BaseEntity {

    private String caminho;
    private boolean principal = false;

    private UUID anuncioId;

    public ImagemAnuncioJpa(UUID id, String caminho, boolean principal,  UUID anuncioId) {
        this.setId(id);
        this.caminho = caminho;
        this.principal = principal;
        this.anuncioId = anuncioId;
    }
}
