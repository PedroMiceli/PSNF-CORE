package com.psnf.api.psnf.domain.anuncio.model;

/**
 * Item da Nomenclatura Comum do Mercosul, usado na nota fiscal para classificar o produto.
 *
 * @param codigo    código de 8 dígitos formatado ("7318.15.00")
 * @param descricao descrição oficial do item
 */
public record Ncm(String codigo, String descricao) {
}
