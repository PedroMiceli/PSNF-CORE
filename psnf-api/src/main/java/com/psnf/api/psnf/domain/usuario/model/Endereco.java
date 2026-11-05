package com.psnf.api.psnf.domain.usuario.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class Endereco extends BaseDomain {

    /** Siglas das 27 unidades federativas do Brasil. */
    public static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String cep;
    private final String uf;

    private Endereco(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String logradouro, String numero, String complemento, String bairro, String cidade, String cep, String uf) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.cep = cep;
        this.uf = uf;
    }

    public static Endereco criar(String logradouro, String numero, String complemento, String bairro, String cidade, String cep, String uf) {
        return new Endereco(null, LocalDateTime.now(), null, null, logradouro, numero, complemento, bairro, cidade, cep, normalizarUf(uf));
    }

    public static Endereco restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String logradouro, String numero, String complemento, String bairro, String cidade, String cep, String uf) {
        return new Endereco(id, dataCadastro, dataAlteracao, dataExcluido, logradouro, numero, complemento, bairro, cidade, cep, uf);
    }

    /** Valida a UF contra as 27 siglas e devolve em maiúsculas (ex.: "sp" -> "SP"). */
    private static String normalizarUf(String uf) {
        String sigla = uf == null ? "" : uf.trim().toUpperCase();
        if (!UFS.contains(sigla)) {
            throw new IllegalArgumentException("UF inválida: '" + (uf == null ? "" : uf) + "'. Informe a sigla do estado, ex.: SP");
        }
        return sigla;
    }

    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getCep() { return cep; }
    public String getUf() { return uf; }
}
