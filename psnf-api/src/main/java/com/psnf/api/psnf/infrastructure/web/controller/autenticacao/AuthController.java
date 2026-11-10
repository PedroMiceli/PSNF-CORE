package com.psnf.api.psnf.infrastructure.web.controller.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.model.ClienteInfo;
import com.psnf.api.psnf.domain.autenticacao.model.SessaoAutenticada;
import com.psnf.api.psnf.domain.autenticacao.port.in.AutenticacaoUseCase;
import com.psnf.api.psnf.infrastructure.security.CookiesAutenticacao;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Login e sessão.
 *
 * <p>Navegador (front): POST /auth/login, /auth/refresh e /auth/logout trabalham com cookies HttpOnly;
 * o corpo da resposta traz só os dados do usuário.
 *
 * <p>Scripts, testes e apps: POST /auth/token devolve os tokens no corpo; use
 * {@code Authorization: Bearer <accessToken>} e renove com POST /auth/refresh {"refreshToken": "..."}.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    public record LoginRequest(@NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
                               @NotBlank(message = "Informe a senha") String senha) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record TokensResponse(String accessToken, long accessExpiraEmSegundos, String refreshToken,
                                 UsuarioResponse usuario) {
        static TokensResponse from(SessaoAutenticada s) {
            return new TokensResponse(s.accessToken().valor(), s.accessToken().validade().toSeconds(),
                    s.refreshToken() == null ? null : s.refreshToken().valor(), UsuarioResponse.from(s.usuario()));
        }
    }

    private final AutenticacaoUseCase autenticacao;
    private final CookiesAutenticacao cookies;

    /** Garante o cookie XSRF-TOKEN: o front chama antes do primeiro POST (ex.: o login). */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName());
    }

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody @Valid LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        SessaoAutenticada sessao = autenticacao.entrar(req.email(), req.senha(), cliente(request));
        cookies.gravar(response, sessao);
        return UsuarioResponse.from(sessao.usuario());
    }

    @PostMapping("/token")
    public TokensResponse token(@RequestBody @Valid LoginRequest req, HttpServletRequest request) {
        return TokensResponse.from(autenticacao.entrar(req.email(), req.senha(), cliente(request)));
    }

    /** refresh token do cookie (navegador) ou do corpo (scripts/apps); a resposta segue o mesmo formato */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody(required = false) RefreshRequest body,
                                     HttpServletRequest request, HttpServletResponse response) {
        String doCookie = CookiesAutenticacao.ler(request, CookiesAutenticacao.REFRESH);
        if (doCookie == null && body != null && body.refreshToken() != null) {
            return ResponseEntity.ok(TokensResponse.from(autenticacao.renovar(body.refreshToken(), cliente(request))));
        }
        try {
            SessaoAutenticada sessao = autenticacao.renovar(doCookie, cliente(request));
            cookies.gravar(response, sessao);
            return ResponseEntity.ok(UsuarioResponse.from(sessao.usuario()));
        } catch (RuntimeException e) {
            cookies.limpar(response); // sessão morta: não adianta o navegador continuar mandando
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest body,
                                       HttpServletRequest request, HttpServletResponse response) {
        String token = CookiesAutenticacao.ler(request, CookiesAutenticacao.REFRESH);
        autenticacao.sair(token != null ? token : body == null ? null : body.refreshToken());
        cookies.limpar(response);
        // Clear-Site-Data: o navegador também descarta caches e storage desta origem
        return ResponseEntity.noContent().header("Clear-Site-Data", "\"cache\", \"storage\"").build();
    }

    /** encerra todas as sessões do usuário logado (todos os dispositivos), inclusive esta */
    @PostMapping("/logout-todos")
    public ResponseEntity<Void> logoutTodos(HttpServletResponse response) {
        autenticacao.sairDeTodos(UsuarioAutenticadoUtil.obterUsuarioLogadoId());
        cookies.limpar(response);
        return ResponseEntity.noContent().build();
    }

    private static ClienteInfo cliente(HttpServletRequest request) {
        return new ClienteInfo(request.getRemoteAddr(), request.getHeader(HttpHeaders.USER_AGENT));
    }
}
