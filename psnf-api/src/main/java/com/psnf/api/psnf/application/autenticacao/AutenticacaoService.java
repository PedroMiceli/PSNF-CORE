package com.psnf.api.psnf.application.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.exception.CredenciaisInvalidasException;
import com.psnf.api.psnf.domain.autenticacao.exception.MuitasTentativasException;
import com.psnf.api.psnf.domain.autenticacao.exception.SessaoInvalidaException;
import com.psnf.api.psnf.domain.autenticacao.model.ClienteInfo;
import com.psnf.api.psnf.domain.autenticacao.model.RefreshToken;
import com.psnf.api.psnf.domain.autenticacao.model.SessaoAutenticada;
import com.psnf.api.psnf.domain.autenticacao.model.TokenEmitido;
import com.psnf.api.psnf.domain.autenticacao.port.in.AutenticacaoUseCase;
import com.psnf.api.psnf.domain.autenticacao.port.out.AccessTokenPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.GeradorTokenPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.LimiteTentativasLoginPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.RefreshTokenRepositoryPort;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AutenticacaoService implements AutenticacaoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoService.class);

    /** duas abas renovando juntas: a segunda chega com o token que a primeira acabou de trocar */
    private static final Duration JANELA_RENOVACAO_SIMULTANEA = Duration.ofSeconds(30);

    private final UsuarioRepositoryPort usuarios;
    private final SenhaEncoderPort senhas;
    private final AccessTokenPort accessTokens;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final GeradorTokenPort gerador;
    private final LimiteTentativasLoginPort tentativas;
    private final Duration validadeRefresh;
    private final Clock relogio;
    /** hash de uma senha qualquer: e-mail inexistente também paga o custo do BCrypt (mesmo tempo de resposta) */
    private final String hashFicticio;

    public AutenticacaoService(UsuarioRepositoryPort usuarios, SenhaEncoderPort senhas, AccessTokenPort accessTokens,
                               RefreshTokenRepositoryPort refreshTokens, GeradorTokenPort gerador,
                               LimiteTentativasLoginPort tentativas,
                               @Value("${psnf.auth.refresh-validade:30d}") Duration validadeRefresh,
                               Optional<Clock> relogio) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
        this.gerador = gerador;
        this.tentativas = tentativas;
        this.validadeRefresh = validadeRefresh;
        this.relogio = relogio.orElse(Clock.systemDefaultZone());
        this.hashFicticio = senhas.criptografar(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public SessaoAutenticada entrar(String email, String senha, ClienteInfo cliente) {
        String chave = email == null ? "" : email.trim().toLowerCase();
        // bloqueado: recusa ANTES de conferir a senha (senão a adivinhação continuaria, só sem resposta)
        long espera = tentativas.esperaSegundos(chave);
        if (espera > 0) throw new MuitasTentativasException(espera);

        Optional<Usuario> encontrado = chave.isEmpty() ? Optional.empty() : usuarios.buscarPorEmail(chave);
        String hash = encontrado.map(Usuario::getSenha).orElse(hashFicticio);
        boolean senhaConfere = senha != null && senhas.confere(senha, hash);
        Optional<Usuario> autenticado = encontrado.filter(u -> senhaConfere && u.getDataExcluido() == null);
        if (autenticado.isEmpty()) {
            tentativas.registrarFalha(chave);
            throw new CredenciaisInvalidasException();
        }
        tentativas.registrarSucesso(chave);
        return abrirSessao(autenticado.get(), UUID.randomUUID(), cliente);
    }

    // noRollbackFor: ao detectar reuso, a revogação da família precisa ficar gravada mesmo lançando 401
    @Override
    @Transactional(noRollbackFor = SessaoInvalidaException.class)
    public SessaoAutenticada renovar(String valor, ClienteInfo cliente) {
        if (valor == null || valor.isBlank()) throw new SessaoInvalidaException();
        LocalDateTime agora = LocalDateTime.now(relogio);
        RefreshToken token = refreshTokens.buscarPorHash(gerador.hash(valor)).orElseThrow(SessaoInvalidaException::new);

        if (token.revogado() || token.expirado(agora)) throw new SessaoInvalidaException();

        if (token.usado() || !refreshTokens.marcarUsado(token.id(), agora)) {
            RefreshToken atual = token.usado() ? token : refreshTokens.buscarPorHash(token.tokenHash()).orElse(token);
            if (atual.usadoHaMenosDe(JANELA_RENOVACAO_SIMULTANEA, agora)) {
                Usuario usuario = usuarios.buscarPorId(token.usuarioId());
                return new SessaoAutenticada(usuario, accessTokens.emitir(usuario), null);
            }
            // token já trocado há tempo e apresentado de novo: foi copiado. Encerra o login inteiro.
            logger.warn("Reuso de refresh token detectado (usuário {}, família {}, IP {}): sessão revogada",
                    token.usuarioId(), token.familia(), cliente.ip());
            refreshTokens.revogarFamilia(token.familia(), agora);
            throw new SessaoInvalidaException();
        }

        Usuario usuario = usuarios.buscarPorId(token.usuarioId());
        if (usuario.getDataExcluido() != null) throw new SessaoInvalidaException();
        return abrirSessao(usuario, token.familia(), cliente);
    }

    @Override
    @Transactional
    public void sair(String valor) {
        if (valor == null || valor.isBlank()) return;
        refreshTokens.buscarPorHash(gerador.hash(valor))
                .ifPresent(t -> refreshTokens.revogarFamilia(t.familia(), LocalDateTime.now(relogio)));
    }

    @Override
    @Transactional
    public void sairDeTodos(UUID usuarioId) {
        refreshTokens.revogarDoUsuario(usuarioId, LocalDateTime.now(relogio));
    }

    private SessaoAutenticada abrirSessao(Usuario usuario, UUID familia, ClienteInfo cliente) {
        String valor = gerador.gerar();
        refreshTokens.salvar(RefreshToken.novo(usuario.getId(), familia, gerador.hash(valor), validadeRefresh,
                cliente.ip(), cliente.userAgent(), LocalDateTime.now(relogio)));
        TokenEmitido access = accessTokens.emitir(usuario);
        return new SessaoAutenticada(usuario, access, new TokenEmitido(valor, validadeRefresh));
    }
}
