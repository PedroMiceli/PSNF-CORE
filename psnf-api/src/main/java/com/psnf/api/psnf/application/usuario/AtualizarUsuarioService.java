package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.AtualizarUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.command.AtualizarUsuarioCommand;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarUsuarioService implements AtualizarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final VerificacaoCnpj verificacaoCnpj;

    @Override
    @Transactional
    public Usuario atualizarUsuario(AtualizarUsuarioCommand command) {
        Usuario usuario = usuarioRepositoryPort.buscarPorId(command.usuarioId());
        usuario.atualizarDados(command.nome(), command.telefone(), command.imagem());
        // ausente (null) mantém o CNPJ; texto vazio remove. Assim um cliente que não conhece o campo
        // (script antigo, outro app) não apaga o CNPJ ao atualizar só o nome ou a foto.
        verificacaoCnpj.aplicar(usuario, command.cnpj());
        return usuarioRepositoryPort.save(usuario);
    }
}
