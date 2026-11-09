package com.psnf.api.psnf.domain.novidades.port.in;

import com.psnf.api.psnf.domain.novidades.model.Novidades;

import java.util.UUID;

public interface ConsultarNovidadesUseCase {

    Novidades consultar(UUID usuarioId);
}
