package com.psnf.api.psnf.domain.usuario.port.in.mapper;

import com.psnf.api.psnf.domain.usuario.model.Endereco;
import com.psnf.api.psnf.domain.usuario.port.in.command.EnderecoCommand;

import java.util.List;

public class EnderecoCommandMapper {

    public static Endereco toDomain(EnderecoCommand c) {
        return Endereco.criar(c.logradouro(), c.numero(), c.complemento(), c.bairro(), c.cidade(), c.cep(), c.uf());
    }

    public static List<Endereco> toDomain(List<EnderecoCommand> enderecos) {
        return enderecos.stream().map(EnderecoCommandMapper::toDomain).toList();
    }
}
