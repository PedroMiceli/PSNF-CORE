package com.psnf.api.psnf.infrastructure.web.controller.anuncio.mapper;

import com.psnf.api.psnf.domain.anuncio.port.in.command.AtualizarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarImagemAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarValorAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarVariacaoAnuncioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.AnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.ImagemAnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.ValorRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.VariacaoRequest;

import java.util.List;
import java.util.UUID;

public class AnuncioRequestMapper {

    public static CadastrarAnuncioCommand toCommand(AnuncioRequest request, UUID vendedorId) {
        return new CadastrarAnuncioCommand(
                vendedorId,
                request.titulo(),
                request.descricao(),
                request.categoriaId(),
                valores(request),
                imagens(request),
                variacoes(request)
        );
    }

    public static AtualizarAnuncioCommand toCommand(UUID anuncioId, UUID usuarioId, AnuncioRequest request) {
        return new AtualizarAnuncioCommand(
                anuncioId,
                usuarioId,
                request.titulo(),
                request.descricao(),
                request.categoriaId(),
                valores(request),
                imagens(request),
                variacoes(request)
        );
    }

    private static List<CadastrarValorAnuncioCommand> valores(AnuncioRequest request) {
        return request.valores().stream().map(AnuncioRequestMapper::toValorCommand).toList();
    }

    private static List<CadastrarImagemAnuncioCommand> imagens(AnuncioRequest request) {
        return request.imagens().stream().map(AnuncioRequestMapper::toImagemCommand).toList();
    }

    private static List<CadastrarVariacaoAnuncioCommand> variacoes(AnuncioRequest request) {
        if (request.variacoes() == null) return List.of();
        return request.variacoes().stream().map(AnuncioRequestMapper::toVariacaoCommand).toList();
    }

    private static CadastrarValorAnuncioCommand toValorCommand(ValorRequest r) {
        return new CadastrarValorAnuncioCommand(r.valorUnidade(), r.quantidadeMinima());
    }

    private static CadastrarImagemAnuncioCommand toImagemCommand(ImagemAnuncioRequest r) {
        return new CadastrarImagemAnuncioCommand(r.caminho(), r.isPrincipal());
    }

    private static CadastrarVariacaoAnuncioCommand toVariacaoCommand(VariacaoRequest r) {
        return new CadastrarVariacaoAnuncioCommand(r.variacao());
    }
}
