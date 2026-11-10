package com.psnf.api.psnf.domain.autenticacao.model;

import java.time.Duration;

/** Um token pronto para entregar ao cliente e por quanto tempo vale. */
public record TokenEmitido(String valor, Duration validade) {
}
