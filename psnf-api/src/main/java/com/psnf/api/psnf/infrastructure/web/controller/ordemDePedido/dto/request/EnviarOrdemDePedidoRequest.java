package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EnviarOrdemDePedidoRequest(
        @FutureOrPresent(message = "A previsão de entrega não pode estar no passado")
        LocalDate previsaoDeEntrega,
        /** opcional: transportadora, horário, instruções... */
        @Size(max = 2000, message = "A descrição da entrega deve ter no máximo 2000 caracteres")
        String descricaoEntrega,
        /** opcional: código de envio/rastreio */
        @Size(max = 100, message = "O código de envio deve ter no máximo 100 caracteres")
        String codigoRastreio,
        /** opcional: o comprador retira no fornecedor (sem código de rastreio); ausente = mantém */
        Boolean retiradaNoLocal
) {
}
