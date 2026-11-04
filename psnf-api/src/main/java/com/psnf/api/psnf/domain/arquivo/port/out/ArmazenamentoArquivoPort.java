package com.psnf.api.psnf.domain.arquivo.port.out;

import com.psnf.api.psnf.domain.arquivo.model.ConteudoArquivo;

import java.util.Optional;

/**
 * Porta para o armazenamento de arquivos (object storage).
 * Implementação atual: S3 (AWS SDK) apontando para o MinIO; em produção basta apontar para o S3 da AWS.
 */
public interface ArmazenamentoArquivoPort {

    void salvar(String nome, String contentType, byte[] conteudo);

    Optional<ConteudoArquivo> carregar(String nome);
}
