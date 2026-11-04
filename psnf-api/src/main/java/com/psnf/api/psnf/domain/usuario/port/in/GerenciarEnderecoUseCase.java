package com.psnf.api.psnf.domain.usuario.port.in;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.command.EnderecoCommand;

import java.util.UUID;

public interface GerenciarEnderecoUseCase {

    Usuario adicionarEndereco(UUID usuarioId, EnderecoCommand endereco);

    Usuario removerEndereco(UUID usuarioId, UUID enderecoId);
}
