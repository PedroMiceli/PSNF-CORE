package com.psnf.api.psnf.domain.usuario.port.in;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;

public interface SalvarUsuarioUseCase {

    Usuario salvarUsuario(CadastrarUsuarioCommand usuario);
}
