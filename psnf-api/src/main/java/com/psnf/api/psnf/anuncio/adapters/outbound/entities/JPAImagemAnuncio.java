package com.psnf.api.psnf.anuncio.adapters.outbound.entities;

import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import com.psnf.api.psnf.domain.models.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class JPAImagemAnuncio extends BaseEntity {

    private String caminho;
    private boolean principal = false;

    @ManyToOne
    @JoinColumn(name = "anuncio_id", nullable = false)
    private JPAAnuncio anuncio;
}
