package com.psnf.api.psnf.domain.usuario.model;

/**
 * Endereço encontrado a partir de um CEP. Logradouro e bairro podem vir vazios
 * (CEPs gerais de cidades pequenas não têm rua/bairro).
 */
public record EnderecoConsultado(
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf
) {
}
