package com.psnf.api.psnf.infrastructure.web.controller.anuncio.mapper;

import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarImagemAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarValorAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CriarVariacaoAnuncioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.AnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.ImagemAnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.ValorRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.VariacaoRequest;

public class AnuncioRequestMapper {

    public static CadastrarAnuncio toCommand(AnuncioRequest request) {
        return new CadastrarAnuncio(
                request.titulo(),
                request.descricao(),
                request.categoriaId(),
                request.valores().stream().map(AnuncioRequestMapper::toValorCommand).toList(),
                request.imagens().stream().map(AnuncioRequestMapper::toImagemCommand).toList(),
                request.variacoes().stream().map(AnuncioRequestMapper::toVariacaoCommand).toList()
        );
    }

    private static CadastrarValorAnuncioCommand toValorCommand(ValorRequest r) {
        return new CadastrarValorAnuncioCommand(r.valorUnidade(), r.quantidadeMinima());
    }

    private static CadastrarImagemAnuncioCommand toImagemCommand(ImagemAnuncioRequest r) {
        return new CadastrarImagemAnuncioCommand(r.caminho(), r.principal());
    }

    private static CriarVariacaoAnuncioCommand toVariacaoCommand(VariacaoRequest r) {
        return new CriarVariacaoAnuncioCommand(r.variacao());
    }
}
