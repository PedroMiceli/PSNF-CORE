package com.psnf.api.psnf.application.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.exception.LinkInvalidoException;
import com.psnf.api.psnf.domain.autenticacao.model.TipoTokenUsuario;
import com.psnf.api.psnf.domain.autenticacao.model.TokenUsuario;
import com.psnf.api.psnf.domain.autenticacao.port.in.AutenticacaoUseCase;
import com.psnf.api.psnf.domain.autenticacao.port.in.ContaUseCase;
import com.psnf.api.psnf.domain.autenticacao.port.out.EnvioEmailPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.GeradorTokenPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.LimiteTentativasLoginPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.TokenUsuarioRepositoryPort;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContaService implements ContaUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ContaService.class);
    /** no máximo um e-mail do mesmo tipo por minuto para o mesmo usuário (não encher a caixa de ninguém) */
    private static final Duration INTERVALO_MINIMO_ENVIO = Duration.ofMinutes(1);

    private final UsuarioRepositoryPort usuarios;
    private final TokenUsuarioRepositoryPort tokens;
    private final GeradorTokenPort gerador;
    private final EnvioEmailPort emails;
    private final MensagensConta mensagens;
    private final SenhaEncoderPort senhas;
    private final AutenticacaoUseCase autenticacao;
    private final LimiteTentativasLoginPort tentativas;
    private final Clock relogio;

    public ContaService(UsuarioRepositoryPort usuarios, TokenUsuarioRepositoryPort tokens, GeradorTokenPort gerador,
                        EnvioEmailPort emails, MensagensConta mensagens, SenhaEncoderPort senhas,
                        AutenticacaoUseCase autenticacao, LimiteTentativasLoginPort tentativas, Optional<Clock> relogio) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.gerador = gerador;
        this.emails = emails;
        this.mensagens = mensagens;
        this.senhas = senhas;
        this.autenticacao = autenticacao;
        this.tentativas = tentativas;
        this.relogio = relogio.orElse(Clock.systemDefaultZone());
    }

    @Override
    @Transactional
    public void enviarConfirmacao(Usuario usuario) {
        if (usuario.emailConfirmado()) return;
        String token = novoToken(usuario, TipoTokenUsuario.CONFIRMACAO_EMAIL);
        emails.enviar(mensagens.confirmacao(usuario, token));
    }

    @Override
    @Transactional
    public void confirmarEmail(String valor) {
        LocalDateTime agora = agora();
        TokenUsuario token = usar(valor, TipoTokenUsuario.CONFIRMACAO_EMAIL, agora);
        Usuario usuario = usuarios.buscarPorId(token.usuarioId());
        usuario.confirmarEmail(agora);
        usuarios.save(usuario);
    }

    @Override
    @Transactional
    public void reenviarConfirmacao(String email) {
        usuarios.buscarPorEmail(normalizar(email))
                .filter(u -> !u.emailConfirmado() && podeEnviar(u, TipoTokenUsuario.CONFIRMACAO_EMAIL))
                .ifPresent(this::enviarConfirmacao);
    }

    @Override
    @Transactional
    public void solicitarRedefinicaoSenha(String email) {
        usuarios.buscarPorEmail(normalizar(email))
                .filter(u -> u.getDataExcluido() == null && podeEnviar(u, TipoTokenUsuario.REDEFINICAO_SENHA))
                .ifPresent(u -> emails.enviar(mensagens.redefinicao(u, novoToken(u, TipoTokenUsuario.REDEFINICAO_SENHA))));
    }

    @Override
    @Transactional
    public void redefinirSenha(String valor, String novaSenha) {
        LocalDateTime agora = agora();
        TokenUsuario token = usar(valor, TipoTokenUsuario.REDEFINICAO_SENHA, agora);
        Usuario usuario = usuarios.buscarPorId(token.usuarioId());
        usuario.alterarSenha(senhas.criptografar(novaSenha));
        usuario.confirmarEmail(agora); // recebeu o link: o e-mail é dele
        usuarios.save(usuario);
        tokens.invalidarDoUsuario(usuario.getId(), TipoTokenUsuario.REDEFINICAO_SENHA, agora);
        autenticacao.sairDeTodos(usuario.getId()); // quem estava usando a senha antiga perde o acesso
        tentativas.registrarSucesso(usuario.getEmail()); // libera o login, caso estivesse bloqueado
        emails.enviar(mensagens.senhaAlterada(usuario));
        logger.info("Senha redefinida pelo link de e-mail: usuário {}", usuario.getId());
    }

    private String novoToken(Usuario usuario, TipoTokenUsuario tipo) {
        LocalDateTime agora = agora();
        tokens.invalidarDoUsuario(usuario.getId(), tipo, agora); // só o link mais recente vale
        String valor = gerador.gerar();
        tokens.salvar(TokenUsuario.novo(usuario.getId(), tipo, gerador.hash(valor), agora));
        return valor;
    }

    private TokenUsuario usar(String valor, TipoTokenUsuario tipo, LocalDateTime agora) {
        if (valor == null || valor.isBlank()) throw new LinkInvalidoException();
        TokenUsuario token = tokens.buscarPorHash(gerador.hash(valor), tipo)
                .filter(t -> t.valido(agora))
                .orElseThrow(LinkInvalidoException::new);
        if (!tokens.marcarUsado(token.id(), agora)) throw new LinkInvalidoException(); // clicado duas vezes ao mesmo tempo
        return token;
    }

    private boolean podeEnviar(Usuario u, TipoTokenUsuario tipo) {
        return tokens.ultimoCriadoEm(u.getId(), tipo)
                .map(ultimo -> !agora().isBefore(ultimo.plus(INTERVALO_MINIMO_ENVIO)))
                .orElse(true);
    }

    private LocalDateTime agora() { return LocalDateTime.now(relogio); }

    private static String normalizar(String email) { return email == null ? "" : email.trim().toLowerCase(); }
}
