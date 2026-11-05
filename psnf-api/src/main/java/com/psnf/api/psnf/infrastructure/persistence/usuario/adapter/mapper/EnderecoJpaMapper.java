package com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper;

import com.psnf.api.psnf.domain.usuario.model.Endereco;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.EnderecoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

public class EnderecoJpaMapper {

    public static EnderecoJpa toEntity(Endereco endereco, UsuarioJpa usuario) {
        EnderecoJpa jpa = new EnderecoJpa();
        jpa.restaurarBase(endereco.getId(), endereco.getDataCadastro(), endereco.getDataAlteracao(), endereco.getDataExcluido());
        jpa.setLogradouro(endereco.getLogradouro());
        jpa.setNumero(endereco.getNumero());
        jpa.setComplemento(endereco.getComplemento());
        jpa.setBairro(endereco.getBairro());
        jpa.setCidade(endereco.getCidade());
        jpa.setCep(endereco.getCep());
        jpa.setUf(endereco.getUf());
        jpa.setUsuario(usuario);
        return jpa;
    }

    public static Endereco toDomain(EnderecoJpa jpa) {
        return Endereco.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getLogradouro(),
                jpa.getNumero(),
                jpa.getComplemento(),
                jpa.getBairro(),
                jpa.getCidade(),
                jpa.getCep(),
                jpa.getUf()
        );
    }
}
