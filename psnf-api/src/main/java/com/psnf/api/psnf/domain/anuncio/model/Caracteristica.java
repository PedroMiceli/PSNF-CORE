package com.psnf.api.psnf.domain.anuncio.model;

/**
 * Item da ficha técnica do produto (ex.: "Material" = "Aço galvanizado").
 */
public record Caracteristica(String nome, String valor) {

    public static final int MAXIMO_POR_ANUNCIO = 50;

    public Caracteristica {
        nome = CodigosProduto.texto(nome, 60, "Nome da característica");
        valor = CodigosProduto.texto(valor, 300, "Valor da característica");
        if (nome == null || valor == null) {
            throw new IllegalArgumentException("Cada característica da ficha técnica precisa de nome e valor");
        }
    }
}
