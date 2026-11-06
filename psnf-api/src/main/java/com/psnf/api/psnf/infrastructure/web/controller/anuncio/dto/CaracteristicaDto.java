package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto;

import com.psnf.api.psnf.domain.anuncio.model.Caracteristica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Item da ficha técnica: "Material" = "Aço galvanizado". */
public record CaracteristicaDto(
        @NotBlank(message = "Informe o nome da característica") @Size(max = 60, message = "Máximo de 60 caracteres") String nome,
        @NotBlank(message = "Informe o valor da característica") @Size(max = 300, message = "Máximo de 300 caracteres") String valor
) {
    public Caracteristica toDomain() {
        return new Caracteristica(nome, valor);
    }

    public static CaracteristicaDto from(Caracteristica c) {
        return new CaracteristicaDto(c.nome(), c.valor());
    }
}
