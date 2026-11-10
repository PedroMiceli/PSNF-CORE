package com.psnf.api.psnf.domain.auditoria.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @param usuarioId nulo quando o evento não é de uma conta (ex.: login com e-mail inexistente)
 * @param ip        preenchido pela infraestrutura a partir da requisição atual
 */
public record EventoAuditoria(UUID id, UUID usuarioId, TipoEventoAuditoria tipo, String detalhe,
                              String ip, String userAgent, LocalDateTime criadoEm) {
}
