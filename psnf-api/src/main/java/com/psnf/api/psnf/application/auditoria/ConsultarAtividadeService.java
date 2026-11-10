package com.psnf.api.psnf.application.auditoria;

import com.psnf.api.psnf.domain.auditoria.model.EventoAuditoria;
import com.psnf.api.psnf.domain.auditoria.port.in.ConsultarAtividadeUseCase;
import com.psnf.api.psnf.domain.auditoria.port.out.AuditoriaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarAtividadeService implements ConsultarAtividadeUseCase {

    private static final int LIMITE = 30;
    private final AuditoriaPort auditoria;

    @Override
    @Transactional(readOnly = true)
    public List<EventoAuditoria> recentes(UUID usuarioId) {
        return auditoria.recentesDoUsuario(usuarioId, LIMITE);
    }
}
