package com.psnf.api.psnf.application.novidades;

import com.psnf.api.psnf.domain.novidades.model.Novidades;
import com.psnf.api.psnf.domain.novidades.port.in.ConsultarNovidadesUseCase;
import com.psnf.api.psnf.domain.novidades.port.out.NovidadesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarNovidadesService implements ConsultarNovidadesUseCase {

    private final NovidadesPort novidadesPort;

    @Override
    @Transactional(readOnly = true)
    public Novidades consultar(UUID usuarioId) {
        return new Novidades(novidadesPort.negociacoes(usuarioId), novidadesPort.ordens(usuarioId), novidadesPort.incidentes(usuarioId));
    }
}
