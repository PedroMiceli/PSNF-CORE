package com.psnf.api.psnf.infrastructure.web.controller.anuncio.mapper;

import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarImagemAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarValorAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarVariacaoAnuncioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.AnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.ImagemAnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.ValorRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.VariacaoRequest;

public class AnuncioRequestMapper {

    public static CadastrarAnuncioCommand toCommand(AnuncioRequest request) {
        return new CadastrarAnuncioCommand(
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

    private static CadastrarVariacaoAnuncioCommand toVariacaoCommand(VariacaoRequest r) {
        return new CadastrarVariacaoAnuncioCommand(r.variacao());
    }
}
