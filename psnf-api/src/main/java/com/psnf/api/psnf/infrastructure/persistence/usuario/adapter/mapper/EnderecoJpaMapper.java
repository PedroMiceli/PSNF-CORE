package com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper;

import com.psnf.api.psnf.domain.usuario.model.Endereco;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.EnderecoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

public class EnderecoJpaMapper {

    public static EnderecoJpa toEntity(Endereco endereco, UsuarioJpa usuario) {

        return new EnderecoJpa(
                endereco.getId(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep(),
                endereco.getUf(),
                usuario
        );
    }

    public static Endereco toDomain(EnderecoJpa enderecoJpa) {
        return new Endereco(
                enderecoJpa.getId(),
                enderecoJpa.getDataCadastro(),
                enderecoJpa.getDataAlteracao(),
                enderecoJpa.getDataExcluido(),
                enderecoJpa.getLogradouro(),
                enderecoJpa.getNumero(),
                enderecoJpa.getComplemento(),
                enderecoJpa.getBairro(),
                enderecoJpa.getCidade(),
                enderecoJpa.getEstado(),
                enderecoJpa.getCep(),
                enderecoJpa.getUf()
        );
    }
}
