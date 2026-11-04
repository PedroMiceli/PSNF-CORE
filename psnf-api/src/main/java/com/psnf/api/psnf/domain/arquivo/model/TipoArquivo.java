package com.psnf.api.psnf.domain.arquivo.model;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * Tipos de arquivo aceitos no upload. O tipo é detectado pelo CONTEÚDO (assinatura / "magic bytes"),
 * nunca pela extensão ou pelo Content-Type enviados pelo cliente, que podem ser falsificados.
 */
public enum TipoArquivo {

    JPEG("image/jpeg", "jpg", true),
    PNG("image/png", "png", true),
    WEBP("image/webp", "webp", true),
    GIF("image/gif", "gif", true),
    PDF("application/pdf", "pdf", false),
    XML("application/xml", "xml", false);

    public static final long TAMANHO_MAXIMO_IMAGEM = 5L * 1024 * 1024;     // 5 MB
    public static final long TAMANHO_MAXIMO_DOCUMENTO = 10L * 1024 * 1024; // 10 MB

    private final String contentType;
    private final String extensao;
    private final boolean imagem;

    TipoArquivo(String contentType, String extensao, boolean imagem) {
        this.contentType = contentType;
        this.extensao = extensao;
        this.imagem = imagem;
    }

    public static Optional<TipoArquivo> detectar(byte[] c) {
        if (c == null || c.length < 4) return Optional.empty();
        if (comeca(c, 0, 0xFF, 0xD8, 0xFF)) return Optional.of(JPEG);
        if (comeca(c, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) return Optional.of(PNG);
        if (comeca(c, 0, 'G', 'I', 'F', '8')) return Optional.of(GIF);
        if (c.length >= 12 && comeca(c, 0, 'R', 'I', 'F', 'F') && comeca(c, 8, 'W', 'E', 'B', 'P')) return Optional.of(WEBP);
        if (comeca(c, 0, '%', 'P', 'D', 'F', '-')) return Optional.of(PDF);
        if (ehXml(c)) return Optional.of(XML);
        return Optional.empty();
    }

    /** NF-e em XML: aceita BOM UTF-8 e espaços antes do "<?xml". */
    private static boolean ehXml(byte[] c) {
        int i = comeca(c, 0, 0xEF, 0xBB, 0xBF) ? 3 : 0;
        while (i < c.length && Character.isWhitespace(c[i])) i++;
        String inicio = new String(c, i, Math.min(5, c.length - i), StandardCharsets.US_ASCII);
        return inicio.equals("<?xml");
    }

    private static boolean comeca(byte[] c, int offset, int... assinatura) {
        if (c.length < offset + assinatura.length) return false;
        for (int i = 0; i < assinatura.length; i++) {
            if ((c[offset + i] & 0xFF) != assinatura[i]) return false;
        }
        return true;
    }

    public static Optional<TipoArquivo> porExtensao(String extensao) {
        return Arrays.stream(values()).filter(t -> t.extensao.equalsIgnoreCase(extensao)).findFirst();
    }

    public long tamanhoMaximo() {
        return imagem ? TAMANHO_MAXIMO_IMAGEM : TAMANHO_MAXIMO_DOCUMENTO;
    }

    public String getContentType() { return contentType; }
    public String getExtensao() { return extensao; }
    public boolean isImagem() { return imagem; }
}
