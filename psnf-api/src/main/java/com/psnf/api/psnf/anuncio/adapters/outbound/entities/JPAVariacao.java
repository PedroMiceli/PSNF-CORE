package com.psnf.api.psnf.anuncio.adapters.outbound.entities;

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
public class JPAVariacao extends BaseEntity {

    private String variacao;

    @ManyToOne
    @JoinColumn(name = "anuncio_id", nullable = false)
    private JPAAnuncio anuncio;
}
