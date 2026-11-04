package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrdemDePedidoResumoResponse(
        UUID id,
        String codigo,
        EnumStatusOrdemDePedido status,
        LocalDateTime dataCadastro,
        LocalDate previsaoDeEntrega,
        String compradorNome,
        String vendedorNome,
        int quantidadeItens,
        BigDecimal valorTotal
) {
    public static OrdemDePedidoResumoResponse from(OrdemDePedido o) {
        return new OrdemDePedidoResumoResponse(
                o.getId(),
                o.getCodigo(),
                o.getStatus(),
                o.getDataCadastro(),
                o.getPrevisaoDeEntrega(),
                o.getComprador().getNome(),
                o.getVendedor().getNome(),
                o.getItens().size(),
                o.getValorTotal()
        );
    }
}
