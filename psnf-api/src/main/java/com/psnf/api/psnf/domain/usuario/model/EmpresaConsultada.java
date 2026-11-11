package com.psnf.api.psnf.domain.usuario.model;

/**
 * Dados públicos de um CNPJ na Receita Federal (consulta pela BrasilAPI).
 *
 * @param situacao situação cadastral: ATIVA, BAIXADA, INAPTA, SUSPENSA ou NULA
 */
public record EmpresaConsultada(String cnpj, String razaoSocial, String nomeFantasia, String situacao,
                                String cep, String logradouro, String numero, String complemento,
                                String bairro, String cidade, String uf) {

    public boolean ativa() { return "ATIVA".equalsIgnoreCase(situacao); }
}
