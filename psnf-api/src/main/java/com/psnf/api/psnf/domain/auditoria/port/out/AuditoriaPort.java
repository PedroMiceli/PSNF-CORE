package com.psnf.api.psnf.domain.auditoria.port.out;

import com.psnf.api.psnf.domain.auditoria.model.EventoAuditoria;
import com.psnf.api.psnf.domain.auditoria.model.TipoEventoAuditoria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Registro de auditoria. Grava fora da transação de quem chama: um login que falha (e desfaz a
 * transação dele) também precisa ficar registrado. IP e navegador vêm da requisição atual, pela infraestrutura.
 */
public interface AuditoriaPort {

    void registrar(UUID usuarioId, TipoEventoAuditoria tipo, String detalhe);

    default void registrar(UUID usuarioId, TipoEventoAuditoria tipo) { registrar(usuarioId, tipo, null); }

    /** mais recentes primeiro */
    List<EventoAuditoria> recentesDoUsuario(UUID usuarioId, int limite);

    int apagarAnterioresA(LocalDateTime limite);
}
