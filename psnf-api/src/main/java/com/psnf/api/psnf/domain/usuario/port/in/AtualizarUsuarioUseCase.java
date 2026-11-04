package com.psnf.api.psnf.domain.usuario.port.in;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.command.AtualizarUsuarioCommand;

public interface AtualizarUsuarioUseCase {

    Usuario atualizarUsuario(AtualizarUsuarioCommand command);
}
