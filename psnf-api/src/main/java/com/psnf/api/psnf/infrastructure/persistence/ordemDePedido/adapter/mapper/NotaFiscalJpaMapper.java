package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.NotaFiscal;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.NotaFiscalJpa;

public class NotaFiscalJpaMapper {


    public static NotaFiscalJpa toEntity(NotaFiscal notaFiscal) {
        return new NotaFiscalJpa(
                notaFiscal.getId(),
                notaFiscal.getCaminho()
        );
    }

    //transforma o jpa para domain
    public static NotaFiscal toDomain(NotaFiscalJpa notaFiscalJpa) {
        return NotaFiscal.restaurar(
            notaFiscalJpa.getId(),
                notaFiscalJpa.getDataCadastro(),
                notaFiscalJpa.getDataAlteracao(),
                notaFiscalJpa.getDataExcluido(),
                notaFiscalJpa.getCaminho()
        );
    }


}
