package com.psnf.api.psnf.domain.arquivo.model;

import java.io.InputStream;

/**
 * Conteúdo de um arquivo lido do storage (em stream, para não carregar tudo em memória).
 */
public record ConteudoArquivo(
        String nome,
        String contentType,
        long tamanho,
        InputStream conteudo
) {
}
