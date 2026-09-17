package com.psnf.api.psnf.infrastructure.web.controller.usuario.mapper;

import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.UsuarioRequest;

public class UsuarioRequestMapper {

    public static CadastrarUsuarioCommand toCommand(UsuarioRequest request) {
        return new CadastrarUsuarioCommand(
                request.email(),
                request.senha(),
                request.nome(),
                request.telefone()
        );
    }


}
