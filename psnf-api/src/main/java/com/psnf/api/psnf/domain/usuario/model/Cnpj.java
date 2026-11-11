package com.psnf.api.psnf.domain.usuario.model;

/**
 * CNPJ: 12 caracteres de identificação + 2 dígitos verificadores.
 *
 * <p>Desde julho de 2026 a Receita Federal emite também o CNPJ alfanumérico: os 12 primeiros caracteres
 * podem ter letras (A-Z); os 2 últimos continuam sendo números. O cálculo dos dígitos é o mesmo módulo 11
 * de sempre, com o valor de cada caractere = código ASCII - 48 (0-9 valem 0-9, A vale 17, B 18...).
 * Os CNPJs só numéricos já existentes continuam válidos.
 *
 * <p>Guardado sem pontuação e em maiúsculas: "12.ABC.345/01DE-35" -> "12ABC34501DE35".
 */
public final class Cnpj {

    private static final int[] PESOS_DV1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_DV2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Cnpj() {
    }

    /**
     * @return o CNPJ normalizado (14 caracteres), ou null se vier vazio (o CNPJ é opcional)
     * @throws IllegalArgumentException se não for um CNPJ válido (HTTP 400)
     */
    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String c = valor.replaceAll("[.\\-/\\s]", "").toUpperCase();
        if (!c.matches("[0-9A-Z]{12}[0-9]{2}") || c.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CNPJ inválido");
        }
        if (digito(c, PESOS_DV1) != c.charAt(12) - '0' || digito(c, PESOS_DV2) != c.charAt(13) - '0') {
            throw new IllegalArgumentException("CNPJ inválido: confira os números");
        }
        return c;
    }

    public static boolean valido(String valor) {
        try {
            return normalizar(valor) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** "12345678000195" -> "12.345.678/0001-95" */
    public static String formatar(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/"
                + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    /** dígito verificador dos primeiros pesos.length caracteres */
    static int digito(String c, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) soma += (c.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
