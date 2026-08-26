package com.psnf.api.psnf.infrastructure.persistence.shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    /******************************************* ATRIBUTOS *******************************************/
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false)
    private UUID id;

    @Column(updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dataCadastro;

    private LocalDateTime dataAlteracao;

    private LocalDateTime dataExcluido;

    /******************************************* METODOS *******************************************/

    public void setIdAndDate(UUID id){
        this.setId(id);

        if (id == null)
            this.dataCadastro = LocalDateTime.now();
        else
            this.dataAlteracao = LocalDateTime.now();
    }

    public void confirmarExclusao() { this.dataExcluido = LocalDateTime.now(); }

    public boolean naoExcluido() { return this.dataExcluido == null; }
}
