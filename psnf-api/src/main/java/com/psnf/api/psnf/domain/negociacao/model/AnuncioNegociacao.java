package com.psnf.api.psnf.domain.negociacao.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

/** Anúncio do vendedor incluído na negociação depois da abordagem (o comprador decidiu levar mais produtos). */
public class AnuncioNegociacao extends BaseDomain {

    private final UUID anuncioId;
    private final UUID adicionadoPorId;

    private AnuncioNegociacao(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                              UUID anuncioId, UUID adicionadoPorId) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.anuncioId = anuncioId;
        this.adicionadoPorId = adicionadoPorId;
    }

    static AnuncioNegociacao criar(UUID anuncioId, UUID adicionadoPorId) {
        return new AnuncioNegociacao(null, LocalDateTime.now(), null, null, anuncioId, adicionadoPorId);
    }

    public static AnuncioNegociacao restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                              UUID anuncioId, UUID adicionadoPorId) {
        return new AnuncioNegociacao(id, dataCadastro, dataAlteracao, dataExcluido, anuncioId, adicionadoPorId);
    }

    public UUID getAnuncioId() { return anuncioId; }
    public UUID getAdicionadoPorId() { return adicionadoPorId; }
}
