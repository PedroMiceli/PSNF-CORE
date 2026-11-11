package com.psnf.api.psnf.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Set;

/**
 * Regras de acesso da API.
 *
 * <ul>
 *   <li>Sem sessão no servidor (STATELESS): cada requisição traz o JWT; qualquer instância atende.</li>
 *   <li>Públicas: login/renovação/logout (/auth/**), o cadastro (POST /usuarios), a consulta de CNPJ
 *       usada nele (GET /cnpj/**) e a vitrine: lista/pesquisa de anúncios (GET /anuncios), categorias e
 *       imagens. Abrir um anúncio, negociar e todo o resto exigem login.</li>
 *   <li>CSRF: como o navegador manda os cookies sozinho, um site malicioso poderia disparar ações em nome
 *       do usuário. O Spring grava um token no cookie XSRF-TOKEN (legível pelo JavaScript do nosso front)
 *       e exige que ele volte no header X-XSRF-TOKEN; outro site não consegue ler o cookie para copiá-lo.
 *       Quem usa o header Authorization (scripts, apps) está fora: não há cookie para abusar.</li>
 *   <li>Access token: header Authorization ou cookie psnf_access (ver {@link CookieParaBearerFilter}).</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    /**
     * CSRF só faz sentido quando o navegador envia os cookies de sessão sozinho: é a "carona" que o
     * ataque usa. Sem esses cookies (scripts com Authorization, POST /auth/token) não há o que proteger.
     * O login exige CSRF mesmo sem cookie: impede um site de logar a vítima na conta do atacante.
     */
    private static final RequestMatcher PRECISA_CSRF = request ->
            !METODOS_SEGUROS.contains(request.getMethod())
                    && request.getHeader("Authorization") == null
                    && ("/auth/login".equals(request.getRequestURI().substring(request.getContextPath().length()))
                        || CookiesAutenticacao.ler(request, CookiesAutenticacao.ACCESS) != null
                        || CookiesAutenticacao.ler(request, CookiesAutenticacao.REFRESH) != null);

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.spa().requireCsrfProtectionMatcher(PRECISA_CSRF))
                .addFilterAfter(new CookieParaBearerFilter(), CsrfFilter.class)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/auth/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        // consulta de CNPJ: usada no cadastro, antes do login
                        .requestMatchers(HttpMethod.GET, "/cnpj/**").permitAll()
                        // vitrine pública: listar e pesquisar anúncios (só a lista; o detalhe /anuncios/{id} exige login),
                        // as categorias e as imagens dos cards (o ArquivoController exige login para PDF/XML)
                        .requestMatchers(HttpMethod.GET, "/anuncios", "/categorias", "/arquivos/*").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .authenticationEntryPoint(RespostasDeSeguranca.naoAutenticado())
                        .accessDeniedHandler(RespostasDeSeguranca.acessoNegado())
                        .jwt(j -> { }))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(RespostasDeSeguranca.naoAutenticado())
                        .accessDeniedHandler(RespostasDeSeguranca.acessoNegado()))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable());
        return http.build();
    }
}
