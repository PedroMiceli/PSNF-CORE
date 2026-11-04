package com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.adapter.mapper;

import com.psnf.api.psnf.domain.ordemDePedido.models.Pagamento;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.OrdemDePedidoJpa;
import com.psnf.api.psnf.infrastructure.persistence.ordemDePedido.entity.PagamentoJpa;

public class PagamentoJpaMapper {

    //transforma o domain para a entidade de infra
    public static PagamentoJpa toEntity(Pagamento pagamento, OrdemDePedidoJpa ordemDePedido) {
        PagamentoJpa jpa = new PagamentoJpa();
        jpa.restaurarBase(pagamento.getId(), pagamento.getDataCadastro(), pagamento.getDataAlteracao(), pagamento.getDataExcluido());
        jpa.setTipoPagamento(pagamento.getTipoPagamento());
        jpa.setValor(pagamento.getValor());
        jpa.setAgencia(pagamento.getAgencia());
        jpa.setCodigoBanco(pagamento.getCodigoBanco());
        jpa.setConta(pagamento.getConta());
        jpa.setNomeReferencia(pagamento.getNomeReferencia());
        jpa.setChavePix(pagamento.getChavePix());
        jpa.setBoleto(pagamento.getBoleto());
        jpa.setOrdemDePedido(ordemDePedido);
        return jpa;
    }

    //transforma o jpa para domain
    public static Pagamento toDomain(PagamentoJpa jpa) {
        return Pagamento.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getTipoPagamento(),
                jpa.getValor(),
                jpa.getAgencia(),
                jpa.getCodigoBanco(),
                jpa.getConta(),
                jpa.getNomeReferencia(),
                jpa.getChavePix(),
                jpa.getBoleto()
        );
    }
}
