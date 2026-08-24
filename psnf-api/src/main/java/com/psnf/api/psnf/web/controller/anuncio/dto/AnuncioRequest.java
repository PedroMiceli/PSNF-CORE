package com.psnf.api.psnf.web.controller.anuncio.dto;

import java.util.List;
import java.util.UUID;

public record AnuncioRequest(
        String titulo,
        String descricao,
        UUID categoriaId,
        List<ValorRequest> valores,
        List<ImagemAnuncioRequest> imagens,
        List<VariacaoRequest> variacoes
) {}
