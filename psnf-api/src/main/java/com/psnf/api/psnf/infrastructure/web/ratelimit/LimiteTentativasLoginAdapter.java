package com.psnf.api.psnf.infrastructure.web.ratelimit;

import com.psnf.api.psnf.domain.autenticacao.port.out.LimiteTentativasLoginPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Token bucket por e-mail que só gasta ficha em senha ERRADA: {@code falhas} erros seguidos são tolerados
 * (erro de digitação), depois o e-mail ganha uma tentativa a cada {@code intervaloMinutos}. Senha certa zera.
 * Para quem tenta adivinhar: 5 tentativas e depois 12 por hora, em vez de milhares por minuto.
 *
 * <p>Em memória, por instância (como o rate limit): com N instâncias, até N vezes mais tentativas. Para
 * um limite global, a mesma ideia vai para o Redis.
 */
@Component
public class LimiteTentativasLoginAdapter implements LimiteTentativasLoginPort {

    private static final int MAX_EMAILS = 100_000;
    private static final long LIMPEZA_A_CADA = TimeUnit.MINUTES.toNanos(5);

    private final int falhas;
    private final double fichasPorMinuto;
    private final Map<String, TokenBucket> baldes = new ConcurrentHashMap<>();
    private final LongSupplier relogio;
    private final AtomicLong ultimaLimpeza;

    @Autowired
    public LimiteTentativasLoginAdapter(@Value("${psnf.auth.login-falhas:5}") int falhas,
                                        @Value("${psnf.auth.login-intervalo-minutos:5}") int intervaloMinutos) {
        this(falhas, intervaloMinutos, System::nanoTime);
    }

    LimiteTentativasLoginAdapter(int falhas, int intervaloMinutos, LongSupplier relogio) {
        this.falhas = falhas;
        this.fichasPorMinuto = 1d / intervaloMinutos;
        this.relogio = relogio;
        this.ultimaLimpeza = new AtomicLong(relogio.getAsLong());
    }

    @Override
    public long esperaSegundos(String email) {
        TokenBucket b = baldes.get(email);
        return b == null ? 0 : b.esperaSegundos(relogio.getAsLong());
    }

    @Override
    public void registrarFalha(String email) {
        long agora = relogio.getAsLong();
        limpar(agora);
        if (baldes.size() >= MAX_EMAILS && !baldes.containsKey(email)) return; // proteção de memória
        baldes.computeIfAbsent(email, k -> new TokenBucket(falhas, fichasPorMinuto, agora)).gastar(agora);
    }

    @Override
    public void registrarSucesso(String email) {
        baldes.remove(email);
    }

    /** baldes cheios de novo (ninguém errou há tempo) equivalem a nenhum: podem sair da memória */
    private void limpar(long agora) {
        long ultima = ultimaLimpeza.get();
        if (agora - ultima < LIMPEZA_A_CADA || !ultimaLimpeza.compareAndSet(ultima, agora)) return;
        baldes.entrySet().removeIf(e -> e.getValue().cheio(agora));
    }
}
