package com.psnf.api.psnf.domain.auditoria.port.in;

import com.psnf.api.psnf.domain.auditoria.model.EventoAuditoria;

import java.util.List;
import java.util.UUID;

public interface ConsultarAtividadeUseCase {
    /** últimos eventos de segurança da própria conta */
    List<EventoAuditoria> recentes(UUID usuarioId);
}
