package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VariacaoRequest(
        @NotBlank(message = "Variação é obrigatória")
        String variacao,
        /* opcionais: códigos próprios da variação */
        @Size(max = 60, message = "SKU deve ter no máximo 60 caracteres") String sku,
        @Pattern(regexp = "^\\s*(\\d{8}|\\d{12}|\\d{13}|\\d{14}|(?i:sem gtin))?\\s*$", message = "GTIN/EAN deve ter 8, 12, 13 ou 14 dígitos")
        String gtin
) {}
