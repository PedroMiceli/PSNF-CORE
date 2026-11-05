package com.psnf.api.psnf.domain.usuario.port.in.command;

public record EnderecoCommand(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String cep,
        String uf
) {
    public EnderecoCommand {
        if (logradouro == null || logradouro.isBlank()) {
            throw new IllegalArgumentException("Logradouro é obrigatório");
        }
        if (cidade == null || cidade.isBlank()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (cep == null || cep.isBlank()) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }
        if (uf == null || uf.isBlank()) {
            throw new IllegalArgumentException("UF é obrigatória");
        }
    }
}
