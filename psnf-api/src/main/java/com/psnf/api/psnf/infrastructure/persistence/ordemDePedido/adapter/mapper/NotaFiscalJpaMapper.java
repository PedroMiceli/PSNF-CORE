package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.NotaFiscal;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.NotaFiscalJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;

public class NotaFiscalJpaMapper {

    public static NotaFiscalJpa toEntity(NotaFiscal notaFiscal, OrdemDePedidoJpa ordemDePedido) {
        NotaFiscalJpa jpa = new NotaFiscalJpa();
        jpa.restaurarBase(notaFiscal.getId(), notaFiscal.getDataCadastro(), notaFiscal.getDataAlteracao(), notaFiscal.getDataExcluido());
        jpa.setCaminho(notaFiscal.getCaminho());
        jpa.setOrdemDePedido(ordemDePedido);
        return jpa;
    }

    //transforma o jpa para domain
    public static NotaFiscal toDomain(NotaFiscalJpa jpa) {
        return NotaFiscal.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getCaminho()
        );
    }
}
