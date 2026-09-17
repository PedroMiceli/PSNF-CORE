package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response;

import com.psnf.api.psnf.domain.usuario.model.Endereco;

import java.util.UUID;

public record EnderecoResponse(
        UUID id,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String uf
) {
    public static EnderecoResponse from(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep(),
                endereco.getUf()
        );
    }
}
