package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.Pagamento;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.PagamentoJpa;

public class PagamentoJpaMapper {

    //transforma o domain para a entidade de infra
    public static PagamentoJpa toEntity(Pagamento pagamento) {
        return new PagamentoJpa(
                pagamento.getId(),
                pagamento.getAgencia(),
                pagamento.getCodigoBanco(),
                pagamento.getConta(),
                pagamento.getNomeReferencia(),
                pagamento.getChavePix(),
                pagamento.getBoleto(),
                pagamento.getValor(),
                pagamento.getTipoPagamento(),
                new OrdemDePedidoJpa(pagamento.getOrdemDePedido())
        );
    }

    //transforma o jpa para domain
    public static Pagamento toDomain(PagamentoJpa pagamentoJpa) {
        return Pagamento.restaurar(
                pagamentoJpa.getId(),
                pagamentoJpa.getDataCadastro(),
                pagamentoJpa.getDataAlteracao(),
                pagamentoJpa.getDataExcluido(),
                pagamentoJpa.getAgencia(),
                pagamentoJpa.getCodigoBanco(),
                pagamentoJpa.getConta(),
                pagamentoJpa.getNomeReferencia(),
                pagamentoJpa.getChavePix(),
                pagamentoJpa.getBoleto(),
                pagamentoJpa.getValor(),
                pagamentoJpa.getTipoPagamento(),
                pagamentoJpa.getOrdemDePedido().getId()
        );
    }
}
