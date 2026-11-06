package com.psnf.api.psnf.domain.anuncio.model;

/**
 * Normalização e validação dos códigos do produto (SKU, GTIN, NCM, CEST).
 * Todos são opcionais: valores vazios viram null.
 */
public final class CodigosProduto {

    private CodigosProduto() {
    }

    public static String texto(String valor, int maximo, String campo) {
        if (valor == null || valor.isBlank()) return null;
        String t = valor.trim();
        if (t.length() > maximo) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + maximo + " caracteres");
        }
        return t;
    }

    /** SKU: código interno do vendedor (vai como "código do produto" na NF). */
    public static String sku(String valor) {
        return texto(valor, 60, "SKU");
    }

    /**
     * GTIN/EAN (código de barras): 8, 12, 13 ou 14 dígitos com dígito verificador válido.
     * "SEM GTIN" (como a NF-e aceita) é tratado como não informado.
     */
    public static String gtin(String valor) {
        if (valor == null || valor.isBlank() || valor.trim().equalsIgnoreCase("SEM GTIN")) return null;
        String d = valor.replaceAll("\\s", "");
        if (!d.matches("\\d{8}|\\d{12}|\\d{13}|\\d{14}")) {
            throw new IllegalArgumentException("GTIN/EAN inválido: deve ter 8, 12, 13 ou 14 dígitos");
        }
        int soma = 0;
        for (int i = d.length() - 2, peso = 3; i >= 0; i--, peso = peso == 3 ? 1 : 3) {
            soma += (d.charAt(i) - '0') * peso;
        }
        int verificador = (10 - soma % 10) % 10;
        if (verificador != d.charAt(d.length() - 1) - '0') {
            throw new IllegalArgumentException("GTIN/EAN inválido: dígito verificador não confere (" + d + ")");
        }
        return d;
    }

    /** NCM: 8 dígitos, guardado no formato oficial "0000.00.00". */
    public static String ncm(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String d = valor.replaceAll("\\D", "");
        if (d.length() != 8) {
            throw new IllegalArgumentException("NCM inválido: deve ter 8 dígitos (ex.: 8716.80.00)");
        }
        return d.substring(0, 4) + "." + d.substring(4, 6) + "." + d.substring(6);
    }

    /** CEST: 7 dígitos, guardado no formato "00.000.00". */
    public static String cest(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String d = valor.replaceAll("\\D", "");
        if (d.length() != 7) {
            throw new IllegalArgumentException("CEST inválido: deve ter 7 dígitos (ex.: 01.003.00)");
        }
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5);
    }
}
