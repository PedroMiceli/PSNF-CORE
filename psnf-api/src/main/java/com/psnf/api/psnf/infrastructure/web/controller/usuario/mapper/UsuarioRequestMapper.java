package com.psnf.api.psnf.infrastructure.web.controller.usuario.mapper;

import com.psnf.api.psnf.domain.usuario.port.in.command.AtualizarUsuarioCommand;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.domain.usuario.port.in.command.EnderecoCommand;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.AtualizarUsuarioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.EnderecoRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.UsuarioRequest;

import java.util.List;
import java.util.UUID;

public class UsuarioRequestMapper {

    public static CadastrarUsuarioCommand toCommand(UsuarioRequest request) {
        List<EnderecoRequest> enderecos = request.enderecos() == null ? List.of() : request.enderecos();
        return new CadastrarUsuarioCommand(
                request.email(),
                request.senha(),
                request.nome(),
                request.telefone(),
                request.imagem(),
                enderecos.stream().map(UsuarioRequestMapper::toCommand).toList(),
                request.cnpj()
        );
    }

    public static AtualizarUsuarioCommand toCommand(UUID usuarioId, AtualizarUsuarioRequest request) {
        return new AtualizarUsuarioCommand(usuarioId, request.nome(), request.telefone(), request.imagem(), request.cnpj());
    }

    public static EnderecoCommand toCommand(EnderecoRequest r) {
        return new EnderecoCommand(r.logradouro(), r.numero(), r.complemento(), r.bairro(), r.cidade(), r.cep(), r.uf());
    }
}
