package com.psnf.api.psnf.infrastructure.web.ratelimit;

/**
 * Onde ficam os baldes de fichas (token bucket) do rate limit e do freio de tentativas de login.
 *
 * <p>Cada balde é identificado por uma chave e tem {@code capacidade} fichas, reabastecidas em
 * {@code fichasPorMinuto} (pode ser fracionário: 0,2 = uma ficha a cada 5 minutos). Um balde que nunca foi
 * usado, ou que já reabasteceu por completo, vale como cheio.
 *
 * <p>Implementações: {@link BaldesRedis} (compartilhado entre as instâncias da API) e
 * {@link BaldesEmMemoria} (por instância).
 */
public interface ArmazenamentoBaldes {

    /** resultado de uma tentativa: quantas fichas sobraram e, se negado, em quantos segundos haverá uma */
    record Resultado(boolean permitido, int restantes, long esperarSegundos) {}

    /** gasta uma ficha se houver; senão nega e diz quanto esperar */
    Resultado tentarConsumir(String chave, double capacidade, double fichasPorMinuto);

    /** segundos até haver uma ficha, sem gastar nada (0 = há ficha agora) */
    long esperaSegundos(String chave, double capacidade, double fichasPorMinuto);

    /** gasta uma ficha mesmo que não haja (o balde fica em zero) */
    void gastar(String chave, double capacidade, double fichasPorMinuto);

    /** esquece o balde: a chave volta a ter o balde cheio */
    void remover(String chave);
}
