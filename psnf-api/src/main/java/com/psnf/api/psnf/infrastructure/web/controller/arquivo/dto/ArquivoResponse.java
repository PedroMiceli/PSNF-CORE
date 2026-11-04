package com.psnf.api.psnf.infrastructure.web.controller.arquivo.dto;

import com.psnf.api.psnf.domain.arquivo.model.Arquivo;

/**
 * @param caminho valor a ser usado em imagens de anúncio, nota fiscal, imagem de mensagem, foto do usuário...
 */
public record ArquivoResponse(
        String nome,
        String caminho,
        String nomeOriginal,
        String contentType,
        long tamanho
) {
    public static ArquivoResponse from(Arquivo a) {
        return new ArquivoResponse(a.nome(), a.caminho(), a.nomeOriginal(), a.tipo().getContentType(), a.tamanho());
    }
}
