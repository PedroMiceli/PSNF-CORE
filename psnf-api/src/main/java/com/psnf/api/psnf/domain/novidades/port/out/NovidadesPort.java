package com.psnf.api.psnf.domain.novidades.port.out;

import com.psnf.api.psnf.domain.novidades.model.Novidades;

import java.util.UUID;

/** Consultas agregadas (uma por área), feitas direto no banco com índice: sem carregar os objetos. */
public interface NovidadesPort {

    Novidades.Versao negociacoes(UUID usuarioId);

    Novidades.Versao ordens(UUID usuarioId);

    Novidades.Incidentes incidentes(UUID usuarioId);
}
