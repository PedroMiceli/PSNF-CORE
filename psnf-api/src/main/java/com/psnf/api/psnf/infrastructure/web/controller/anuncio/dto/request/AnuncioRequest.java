package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request;

import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.CaracteristicaDto;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.InformacoesProdutoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AnuncioRequest(
        @NotBlank(message = "Título é obrigatório")
        String titulo,
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,
        @NotNull(message = "Categoria é obrigatória")
        UUID categoriaId,
        @NotEmpty(message = "É necessário informar ao menos um valor")
        List<@Valid ValorRequest> valores,
        @NotEmpty(message = "É necessário informar ao menos uma imagem")
        List<@Valid ImagemAnuncioRequest> imagens,
        List<@Valid VariacaoRequest> variacoes,
        /* opcionais: informações complementares do produto e ficha técnica */
        @Valid InformacoesProdutoDto informacoes,
        @Size(max = 50, message = "A ficha técnica aceita no máximo 50 características")
        List<@Valid CaracteristicaDto> caracteristicas
) {}
