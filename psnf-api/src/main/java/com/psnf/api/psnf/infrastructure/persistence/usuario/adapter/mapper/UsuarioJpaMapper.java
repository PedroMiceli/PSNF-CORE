package com.psnf.api.psnf.infrastructure.persistence.usuario.adapter.mapper;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

public class UsuarioJpaMapper {

    public static UsuarioJpa toEntity(Usuario usuario) {
        UsuarioJpa jpa = new UsuarioJpa();
        jpa.restaurarBase(usuario.getId(), usuario.getDataCadastro(), usuario.getDataAlteracao(), usuario.getDataExcluido());
        jpa.setNome(usuario.getNome());
        jpa.setEmail(usuario.getEmail());
        jpa.setSenha(usuario.getSenha());
        jpa.setSecret2fa(usuario.getSecret2fa());
        jpa.setEmailConfirmadoEm(usuario.getEmailConfirmadoEm());
        jpa.setCnpj(usuario.getCnpj());
        jpa.setRazaoSocial(usuario.getRazaoSocial());
        jpa.setCnpjVerificadoEm(usuario.getCnpjVerificadoEm());
        jpa.setImagem(usuario.getImagem());
        jpa.setTelefone(usuario.getTelefone());
        usuario.getEnderecos().stream()
                .map(endereco -> EnderecoJpaMapper.toEntity(endereco, jpa))
                .forEach(jpa.getEnderecos()::add);
        return jpa;
    }

    public static Usuario toDomain(UsuarioJpa jpa) {
        return Usuario.restaurar(
                jpa.getId(),
                jpa.getDataCadastro(),
                jpa.getDataAlteracao(),
                jpa.getDataExcluido(),
                jpa.getNome(),
                jpa.getEmail(),
                jpa.getSenha(),
                jpa.getSecret2fa(),
                jpa.getImagem(),
                jpa.getTelefone(),
                jpa.getEnderecos().stream().map(EnderecoJpaMapper::toDomain).toList()
        ).comEmailConfirmadoEm(jpa.getEmailConfirmadoEm()).comCnpj(jpa.getCnpj())
                .registrarVerificacaoCnpj(jpa.getRazaoSocial(), jpa.getCnpjVerificadoEm());
    }
}
