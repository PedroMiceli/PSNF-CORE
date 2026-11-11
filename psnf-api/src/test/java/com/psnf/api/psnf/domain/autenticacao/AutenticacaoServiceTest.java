package com.psnf.api.psnf.domain.autenticacao;

import com.psnf.api.psnf.application.autenticacao.AutenticacaoService;
import com.psnf.api.psnf.domain.autenticacao.exception.CredenciaisInvalidasException;
import com.psnf.api.psnf.domain.autenticacao.exception.SessaoInvalidaException;
import com.psnf.api.psnf.domain.autenticacao.model.ClienteInfo;
import com.psnf.api.psnf.domain.autenticacao.model.RefreshToken;
import com.psnf.api.psnf.domain.autenticacao.model.SessaoAutenticada;
import com.psnf.api.psnf.domain.autenticacao.model.TokenEmitido;
import com.psnf.api.psnf.domain.autenticacao.port.out.GeradorTokenPort;
import com.psnf.api.psnf.domain.autenticacao.port.out.RefreshTokenRepositoryPort;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutenticacaoServiceTest {

    private static final ClienteInfo CLIENTE = new ClienteInfo("1.1.1.1", "teste");

    /** relógio que o teste avança */
    private static final class RelogioAjustavel extends Clock {
        Instant agora = Instant.parse("2026-01-01T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
        void avancar(Duration d) { agora = agora.plus(d); }
    }

    private final RelogioAjustavel relogio = new RelogioAjustavel();
    private final Map<String, RefreshToken> porHash = new HashMap<>();
    private Usuario usuario = Usuario.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null,
            "Fulano", "fulano@psnf.com", "hash:segredo", null, null, null, List.of())
            .comEmailConfirmadoEm(LocalDateTime.now());
    private AutenticacaoService service;
    private final List<String> eventos = new ArrayList<>();
    private final com.psnf.api.psnf.domain.auditoria.port.out.AuditoriaPort auditoria =
            new com.psnf.api.psnf.domain.auditoria.port.out.AuditoriaPort() {
                public void registrar(UUID u, com.psnf.api.psnf.domain.auditoria.model.TipoEventoAuditoria t, String d) { eventos.add(t.name()); }
                public List<com.psnf.api.psnf.domain.auditoria.model.EventoAuditoria> recentesDoUsuario(UUID u, int l) { return List.of(); }
                public int apagarAnterioresA(LocalDateTime l) { return 0; }
            };
    /** freio por e-mail simplificado: bloqueia depois de 3 falhas */
    private final Map<String, Integer> falhas = new HashMap<>();
    private final com.psnf.api.psnf.domain.autenticacao.port.out.LimiteTentativasLoginPort tentativas =
            new com.psnf.api.psnf.domain.autenticacao.port.out.LimiteTentativasLoginPort() {
                public long esperaSegundos(String e) { return falhas.getOrDefault(e, 0) >= 3 ? 300 : 0; }
                public void registrarFalha(String e) { falhas.merge(e, 1, Integer::sum); }
                public void registrarSucesso(String e) { falhas.remove(e); }
            };

    private void setUp(Usuario outro) {
        this.usuario = outro;
        setUp();
    }

    @BeforeEach
    void setUp() {
        SenhaEncoderPort senhas = new SenhaEncoderPort() {
            public String criptografar(String s) { return "hash:" + s; }
            public boolean confere(String s, String h) { return h.equals("hash:" + s); }
        };
        UsuarioRepositoryPort usuarios = new UsuarioRepositoryPort() {
            public Usuario save(Usuario u) { return u; }
            public Usuario buscarPorId(UUID id) { return usuario; }
            public Map<UUID, Usuario> buscarPorIds(Collection<UUID> ids) { return Map.of(); }
            public boolean existePorEmail(String e) { return false; }
            public boolean cnpjEmUsoPorOutro(String c, UUID u) { return false; }
            public Optional<Usuario> buscarPorEmail(String e) {
                return e.equalsIgnoreCase(usuario.getEmail()) ? Optional.of(usuario) : Optional.empty();
            }
        };
        AtomicInteger seq = new AtomicInteger();
        GeradorTokenPort gerador = new GeradorTokenPort() {
            public String gerar() { return "token-" + seq.incrementAndGet(); }
            public String hash(String t) { return "h(" + t + ")"; }
        };
        RefreshTokenRepositoryPort repo = new RefreshTokenRepositoryPort() {
            public RefreshToken salvar(RefreshToken t) { porHash.put(t.tokenHash(), t); return t; }
            public Optional<RefreshToken> buscarPorHash(String h) { return Optional.ofNullable(porHash.get(h)); }
            public boolean marcarUsado(UUID id, LocalDateTime agora) {
                RefreshToken t = porHash.values().stream().filter(x -> x.id().equals(id)).findFirst().orElseThrow();
                if (t.usado()) return false;
                porHash.put(t.tokenHash(), com(t, agora, t.revogadoEm()));
                return true;
            }
            public void revogarFamilia(UUID familia, LocalDateTime agora) {
                porHash.replaceAll((h, t) -> t.familia().equals(familia) ? com(t, t.usadoEm(), agora) : t);
            }
            public void revogarDoUsuario(UUID u, LocalDateTime agora) {
                porHash.replaceAll((h, t) -> t.usuarioId().equals(u) ? com(t, t.usadoEm(), agora) : t);
            }
            public int apagarExpirados(LocalDateTime limite) { return 0; }
        };
        service = new AutenticacaoService(usuarios, senhas, u -> new TokenEmitido("jwt-" + u.getId(), Duration.ofMinutes(15)),
                repo, gerador, tentativas, auditoria, Duration.ofDays(30), Optional.of(relogio));
    }

    private static RefreshToken com(RefreshToken t, LocalDateTime usado, LocalDateTime revogado) {
        return new RefreshToken(t.id(), t.usuarioId(), t.familia(), t.tokenHash(), t.criadoEm(), t.expiraEm(),
                usado, revogado, t.ip(), t.userAgent());
    }

    @Test
    void loginComSenhaCertaAbreSessaoEGuardaSoOHash() {
        SessaoAutenticada s = service.entrar("FULANO@psnf.com", "segredo", CLIENTE);
        assertThat(s.usuario()).isSameAs(usuario);
        assertThat(s.refreshToken().valor()).isEqualTo("token-1");
        assertThat(porHash).containsOnlyKeys("h(token-1)"); // o valor nunca é guardado
    }

    @Test
    void senhaErradaOuEmailInexistenteDaoAMesmaResposta() {
        assertThatThrownBy(() -> service.entrar("fulano@psnf.com", "errada", CLIENTE))
                .isInstanceOf(CredenciaisInvalidasException.class).hasMessage("E-mail ou senha inválidos");
        assertThatThrownBy(() -> service.entrar("outro@psnf.com", "segredo", CLIENTE))
                .isInstanceOf(CredenciaisInvalidasException.class).hasMessage("E-mail ou senha inválidos");
    }

    @Test
    void renovarTrocaORefreshTokenNaMesmaFamilia() {
        String r1 = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        SessaoAutenticada s2 = service.renovar(r1, CLIENTE);
        assertThat(s2.refreshToken().valor()).isNotEqualTo(r1);
        assertThat(porHash.get("h(" + r1 + ")").usado()).isTrue();
        assertThat(porHash.get("h(" + s2.refreshToken().valor() + ")").familia())
                .isEqualTo(porHash.get("h(" + r1 + ")").familia());
    }

    @Test
    void duasAbasRenovandoJuntasNaoDerrubamASessao() {
        String r1 = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        service.renovar(r1, CLIENTE);
        relogio.avancar(Duration.ofSeconds(5));
        SessaoAutenticada segundaAba = service.renovar(r1, CLIENTE);
        assertThat(segundaAba.accessToken()).isNotNull();
        assertThat(segundaAba.refreshToken()).isNull(); // não emite outro refresh: a primeira aba já trocou o cookie
    }

    @Test
    void reusoDeTokenAntigoRevogaAFamiliaInteira() {
        String r1 = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        String r2 = service.renovar(r1, CLIENTE).refreshToken().valor();
        relogio.avancar(Duration.ofMinutes(5));

        assertThatThrownBy(() -> service.renovar(r1, CLIENTE)).isInstanceOf(SessaoInvalidaException.class);
        assertThatThrownBy(() -> service.renovar(r2, CLIENTE)).isInstanceOf(SessaoInvalidaException.class);
        assertThat(eventos).containsSequence("LOGIN", "SESSAO_ROUBADA");
    }

    @Test
    void sessaoExpiradaOuEncerradaNaoRenova() {
        String r1 = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        relogio.avancar(Duration.ofDays(31));
        assertThatThrownBy(() -> service.renovar(r1, CLIENTE)).isInstanceOf(SessaoInvalidaException.class);

        String r2 = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        service.sair(r2);
        assertThatThrownBy(() -> service.renovar(r2, CLIENTE)).isInstanceOf(SessaoInvalidaException.class);
    }

    @Test
    void sairDeTodosEncerraTodasAsSessoesDoUsuario() {
        String a = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        String b = service.entrar("fulano@psnf.com", "segredo", CLIENTE).refreshToken().valor();
        service.sairDeTodos(usuario.getId());
        assertThatThrownBy(() -> service.renovar(a, CLIENTE)).isInstanceOf(SessaoInvalidaException.class);
        assertThatThrownBy(() -> service.renovar(b, CLIENTE)).isInstanceOf(SessaoInvalidaException.class);
    }

    @Test
    void bloqueadoPorTentativasRecusaAteASenhaCerta() {
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> service.entrar("Fulano@psnf.com", "errada", CLIENTE)).isInstanceOf(CredenciaisInvalidasException.class);
        }
        assertThatThrownBy(() -> service.entrar("fulano@psnf.com", "segredo", CLIENTE))
                .isInstanceOf(com.psnf.api.psnf.domain.autenticacao.exception.MuitasTentativasException.class);
    }

    @Test
    void senhaCertaZeraAsFalhas() {
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> service.entrar("fulano@psnf.com", "errada", CLIENTE)).isInstanceOf(CredenciaisInvalidasException.class);
        }
        service.entrar("fulano@psnf.com", "segredo", CLIENTE);
        assertThat(falhas).doesNotContainKey("fulano@psnf.com");
    }

    @Test
    void emailNaoConfirmadoNaoEntraMasSoDepoisDaSenhaCerta() {
        Usuario novo = Usuario.restaurar(usuario.getId(), LocalDateTime.now(), null, null, "Novo", "fulano@psnf.com",
                "hash:segredo", null, null, null, List.of());
        setUp(novo);
        assertThatThrownBy(() -> service.entrar("fulano@psnf.com", "errada", CLIENTE)).isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> service.entrar("fulano@psnf.com", "segredo", CLIENTE))
                .isInstanceOf(com.psnf.api.psnf.domain.autenticacao.exception.EmailNaoConfirmadoException.class);
    }
}
