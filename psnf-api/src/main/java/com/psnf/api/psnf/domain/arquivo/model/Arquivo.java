package com.psnf.api.psnf.domain.arquivo.model;

import java.util.UUID;

/**
 * Arquivo armazenado. O {@code nome} é gerado pelo sistema (UUID + extensão do tipo detectado):
 * o nome enviado pelo usuário nunca é usado como chave no storage.
 *
 * @param caminho caminho público para baixar o arquivo pela API (é o que se grava em anúncio, NF, etc.)
 */
public record Arquivo(
        String nome,
        String caminho,
        String nomeOriginal,
        TipoArquivo tipo,
        long tamanho
) {
    public static final String PREFIXO_CAMINHO = "/arquivos/";

    public static Arquivo novo(String nomeOriginal, TipoArquivo tipo, long tamanho) {
        String nome = UUID.randomUUID() + "." + tipo.getExtensao();
        return new Arquivo(nome, PREFIXO_CAMINHO + nome, nomeOriginal, tipo, tamanho);
    }

    /** Valida o formato "uuid.extensao" (impede que alguém peça chaves arbitrárias do bucket). */
    public static boolean nomeValido(String nome) {
        if (nome == null) return false;
        int ponto = nome.lastIndexOf('.');
        if (ponto <= 0) return false;
        try {
            UUID.fromString(nome.substring(0, ponto));
        } catch (IllegalArgumentException e) {
            return false;
        }
        return TipoArquivo.porExtensao(nome.substring(ponto + 1)).isPresent()
                && nome.substring(0, ponto).length() == 36;
    }
}
