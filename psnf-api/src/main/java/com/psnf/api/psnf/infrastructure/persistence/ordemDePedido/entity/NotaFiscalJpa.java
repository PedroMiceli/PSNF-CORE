package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity;

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
public class NotaFiscalJpa extends BaseEntity {

    private String caminho;

    public NotaFiscalJpa(UUID id, String caminho) {
        this.setIdAndDate(id);
        this.caminho = caminho;
    }
}
