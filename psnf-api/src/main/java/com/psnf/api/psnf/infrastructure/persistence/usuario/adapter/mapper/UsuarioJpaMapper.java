package com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.adapter.mapper.AnuncioJpaMapper;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

import java.util.List;

public class UsuarioJpaMapper {

    public static UsuarioJpa toEntity(Usuario usuario) {

        return new UsuarioJpa(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getSecret2fa(),
                usuario.getImagem(),
                usuario.getTelefone()
        );
    }

    public static Usuario toDomain(UsuarioJpa usuarioJpa) {

        return new Usuario(
                usuarioJpa.getId(),
                usuarioJpa.getDataCadastro(),
                usuarioJpa.getDataAlteracao(),
                usuarioJpa.getDataExcluido(),
                usuarioJpa.getNome(),
                usuarioJpa.getEmail(),
                usuarioJpa.getSenha(),
                usuarioJpa.getSecret2fa(),
                usuarioJpa.getImagem(),
                usuarioJpa.getTelefone(),
                usuarioJpa.getEnderecos().stream().map(EnderecoJpaMapper::toDomain).toList(),
                usuarioJpa.getAnuncios().stream().map(AnuncioJpaMapper::toDomain).toList(),
                //compras
                List.of(),
                //vendas
                List.of()
        );
    }
}
