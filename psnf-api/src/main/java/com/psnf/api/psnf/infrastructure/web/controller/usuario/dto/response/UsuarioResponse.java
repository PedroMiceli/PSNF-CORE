package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Anúncios e ordens do usuário são consultados nos próprios endpoints:
 * GET /usuarios/{id}/anuncios, GET /ordens-de-pedido/compras e GET /ordens-de-pedido/vendas.
 */
public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String imagem,
        String telefone,
        /** sem pontuação; null = sem CNPJ */
        String cnpj,
        LocalDateTime dataCadastro,
        List<EnderecoResponse> enderecos
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getImagem(),
                usuario.getTelefone(),
                usuario.getCnpj(),
                usuario.getDataCadastro(),
                usuario.getEnderecos().stream().map(EnderecoResponse::from).toList()
        );
    }
}
