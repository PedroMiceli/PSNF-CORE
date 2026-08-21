package com.psnf.api.psnf.anuncio.domain.models;

import com.psnf.api.psnf.domain.models.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class Variacao extends BaseEntity {

    private String variacao;

    private Anuncio anuncio;
}
