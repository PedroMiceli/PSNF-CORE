package com.psnf.api.psnf.domain.arquivo.port.in.command;

import com.psnf.api.psnf.domain.arquivo.exception.ArquivoInvalidoException;

public record EnviarArquivoCommand(
        String nomeOriginal,
        byte[] conteudo
) {
    public EnviarArquivoCommand {
        if (conteudo == null || conteudo.length == 0) {
            throw new ArquivoInvalidoException("O arquivo está vazio");
        }
    }
}
