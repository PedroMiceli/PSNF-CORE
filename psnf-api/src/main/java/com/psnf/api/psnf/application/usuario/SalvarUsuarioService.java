package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.auditoria.model.TipoEventoAuditoria;
import com.psnf.api.psnf.domain.auditoria.port.out.AuditoriaPort;
import com.psnf.api.psnf.domain.autenticacao.port.in.ContaUseCase;
import com.psnf.api.psnf.domain.usuario.exception.CnpjJaCadastradoException;
import com.psnf.api.psnf.domain.usuario.exception.EmailJaCadastradoException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.SalvarUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.domain.usuario.port.in.mapper.EnderecoCommandMapper;
import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalvarUsuarioService implements SalvarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SenhaEncoderPort senhaEncoderPort;
    private final ContaUseCase contaUseCase;
    private final AuditoriaPort auditoria;

    @Override
    @Transactional
    public Usuario salvarUsuario(CadastrarUsuarioCommand command) {
        String email = command.email().trim().toLowerCase();
        if (usuarioRepositoryPort.existePorEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }

        Usuario usuario = Usuario.criar(
                command.nome(),
                email,
                senhaEncoderPort.criptografar(command.senha()),
                command.telefone(),
                command.imagem(),
                EnderecoCommandMapper.toDomain(command.enderecos())
        );

        usuario.definirCnpj(command.cnpj());
        if (usuarioRepositoryPort.cnpjEmUsoPorOutro(usuario.getCnpj(), null)) throw new CnpjJaCadastradoException();
        Usuario salvo = usuarioRepositoryPort.save(usuario);
        contaUseCase.enviarConfirmacao(salvo); // só entra depois de clicar no link
        auditoria.registrar(salvo.getId(), TipoEventoAuditoria.CADASTRO);
        return salvo;
    }
}
