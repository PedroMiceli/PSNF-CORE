package com.psnf.api.psnf.domain.novidades.model;

import java.time.LocalDateTime;

/**
 * Resposta da consulta leve "mudou alguma coisa para mim?", feita periodicamente pelo front.
 * Só traz contadores e "versões" (data da última mudança): a tela compara com o que já tem e só
 * busca os dados completos quando a versão muda. Assim a maioria das consultas custa quase nada.
 *
 * @param negociacoes      quantidade de conversas do usuário e quando a última recebeu mensagem
 * @param ordens           quantidade de ordens do usuário (compras e vendas) e quando a última mudou
 * @param incidentes       mensagens de incidente do outro lado ainda não vistas
 */
public record Novidades(Versao negociacoes, Versao ordens, Incidentes incidentes) {

    /** total + última mudança: muda quando surge um item novo ou um item existente é alterado */
    public record Versao(long total, LocalDateTime ultimaMudanca) {
        public String chave() {
            return total + "-" + (ultimaMudanca == null ? "0" : ultimaMudanca.toString());
        }
    }

    /**
     * @param naoLidas        mensagens não lidas somando todas as ordens
     * @param ordens          quantas ordens têm mensagens não lidas
     * @param ultimaMensagem  data da mensagem não lida mais recente (muda quando chega outra)
     */
    public record Incidentes(long naoLidas, long ordens, LocalDateTime ultimaMensagem) {
        public String chave() {
            return naoLidas + "-" + (ultimaMensagem == null ? "0" : ultimaMensagem.toString());
        }
    }
}
