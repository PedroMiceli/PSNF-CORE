package com.psnf.api.psnf.infrastructure.security;

import com.psnf.api.psnf.domain.autenticacao.model.SessaoAutenticada;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

/**
 * Os tokens do navegador ficam em cookies:
 * <ul>
 *   <li>HttpOnly: JavaScript não lê (um XSS não consegue roubar o token)</li>
 *   <li>Secure: só trafegam em HTTPS (automático: em HTTP de desenvolvimento vão sem, senão o navegador os descarta)</li>
 *   <li>SameSite=Strict: não são enviados em requisições iniciadas por outros sites (CSRF)</li>
 * </ul>
 * O de refresh tem Path restrito às rotas de autenticação: não viaja em toda requisição.
 */
@Component
@RequiredArgsConstructor
public class CookiesAutenticacao {

    public static final String ACCESS = "psnf_access";
    public static final String REFRESH = "psnf_refresh";

    private final AuthProperties props;

    public void gravar(HttpServletRequest request, HttpServletResponse response, SessaoAutenticada sessao) {
        boolean seguro = props.cookieSeguroPara(request);
        adicionar(response, ACCESS, sessao.accessToken().valor(), "/", sessao.accessToken().validade(), seguro);
        if (sessao.refreshToken() != null) {
            adicionar(response, REFRESH, sessao.refreshToken().valor(), props.caminhoRefresh(), sessao.refreshToken().validade(), seguro);
        }
    }

    public void limpar(HttpServletRequest request, HttpServletResponse response) {
        boolean seguro = props.cookieSeguroPara(request);
        adicionar(response, ACCESS, "", "/", Duration.ZERO, seguro);
        adicionar(response, REFRESH, "", props.caminhoRefresh(), Duration.ZERO, seguro);
    }

    public static String ler(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(c -> nome.equals(c.getName()))
                .map(Cookie::getValue).filter(v -> !v.isBlank()).findFirst().orElse(null);
    }

    private void adicionar(HttpServletResponse response, String nome, String valor, String caminho, Duration validade, boolean seguro) {
        ResponseCookie cookie = ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(seguro)
                .sameSite("Strict")
                .path(caminho)
                .maxAge(validade)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
