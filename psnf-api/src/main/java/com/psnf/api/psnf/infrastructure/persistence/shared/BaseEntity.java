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

    @Column(updatable = false, nullable = false)
    private LocalDateTime dataCadastro;

    private LocalDateTime dataAlteracao;

    private LocalDateTime dataExcluido;

    /******************************************* METODOS *******************************************/

    /**
     * Copia os dados de controle vindos do domínio para a entidade.
     */
    public void restaurarBase(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido) {
        this.id = id;
        this.dataCadastro = dataCadastro;
        this.dataAlteracao = dataAlteracao;
        this.dataExcluido = dataExcluido;
    }

    @PrePersist
    protected void aoCriar() {
        if (this.dataCadastro == null) {
            this.dataCadastro = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void aoAlterar() {
        this.dataAlteracao = LocalDateTime.now();
    }

    public void confirmarExclusao() { this.dataExcluido = LocalDateTime.now(); }

    public boolean naoExcluido() { return this.dataExcluido == null; }
}
