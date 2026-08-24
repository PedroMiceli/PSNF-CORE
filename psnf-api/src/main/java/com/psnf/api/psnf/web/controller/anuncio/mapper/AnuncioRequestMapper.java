package com.psnf.api.psnf.web.controller.anuncio.mapper;

import com.psnf.api.psnf.web.controller.anuncio.dto.AnuncioRequest;
import com.psnf.api.psnf.web.controller.anuncio.dto.ValorRequest;

public class AnuncioRequestMapper {

    public static SalvarAnuncioCommand toCommand(AnuncioRequest request) {
        return new SalvarAnuncioCommand(
                request.titulo(),
                request.descricao(),
                request.categoriaId(),
                request.valores().stream().map(AnuncioRequestMapper::toValorCommand).toList(),
                request.imagens().stream().map(AnuncioRequestMapper::toImagemCommand).toList(),
                request.variacoes().stream().map(AnuncioRequestMapper::toVariacaoCommand).toList()
        );
    }

    private static ValorCommand toValorCommand(ValorRequest r) {
        return new ValorCommand(r.preco(), r.quantidade());
    }
}
