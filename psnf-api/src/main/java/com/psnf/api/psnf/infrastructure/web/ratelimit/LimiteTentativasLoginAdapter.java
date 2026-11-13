package com.psnf.api.psnf.infrastructure.web.ratelimit;

import com.psnf.api.psnf.domain.autenticacao.port.out.LimiteTentativasLoginPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Token bucket por e-mail que só gasta ficha em senha ERRADA: {@code falhas} erros seguidos são tolerados
 * (erro de digitação), depois o e-mail ganha uma tentativa a cada {@code intervaloMinutos}. Senha certa zera.
 * Para quem tenta adivinhar: 5 tentativas e depois 12 por hora, em vez de milhares por minuto.
 *
 * <p>Os baldes ficam no mesmo {@link ArmazenamentoBaldes} do rate limit: no Redis, a contagem vale para
 * todas as instâncias juntas (errar em uma e tentar em outra não dá tentativas a mais).
 */
@Component
public class LimiteTentativasLoginAdapter implements LimiteTentativasLoginPort {

    private final int falhas;
    private final double fichasPorMinuto;
    private final ArmazenamentoBaldes baldes;

    public LimiteTentativasLoginAdapter(@Value("${psnf.auth.login-falhas:5}") int falhas,
                                        @Value("${psnf.auth.login-intervalo-minutos:5}") int intervaloMinutos,
                                        ArmazenamentoBaldes baldes) {
        this.falhas = falhas;
        this.fichasPorMinuto = 1d / intervaloMinutos;
        this.baldes = baldes;
    }

    @Override
    public long esperaSegundos(String email) {
        return baldes.esperaSegundos(chave(email), falhas, fichasPorMinuto);
    }

    @Override
    public void registrarFalha(String email) {
        baldes.gastar(chave(email), falhas, fichasPorMinuto);
    }

    @Override
    public void registrarSucesso(String email) {
        baldes.remover(chave(email));
    }

    private static String chave(String email) {
        return "login:" + email;
    }
}
