package com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response.AnuncioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response.OrdemDePedidoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String imagem,
        int telefone,
        List<EnderecoResponse> enderecos,
        List<AnuncioResponse> anuncios,
        List<OrdemDePedidoResponse> ordens
) {

    public static UsuarioResponse from(Usuario usuario){
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getImagem(),
                usuario.getTelefone(),
                usuario.getEnderecos().stream().map(EnderecoResponse::from).toList(),
                //buscar anuncios a parte
                new ArrayList<>(),
                //buscar ordens a parte
                new ArrayList<>()
        );
    }

}
