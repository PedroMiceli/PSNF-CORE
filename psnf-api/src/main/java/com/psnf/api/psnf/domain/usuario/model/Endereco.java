package com.psnf.api.psnf.domain.usuario.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Endereco extends BaseDomain {
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String uf;

    public Endereco(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExclusao, String logradouro, String numero, String complemento, String bairro, String cidade, String estado, String cep, String uf) {
        super(id,dataCadastro, dataAlteracao, dataExclusao);
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.uf = uf;
    }

    public static Endereco criar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExclusao, String logradouro, String numero, String complemento, String bairro, String cidade, String estado, String cep, String uf) {
        return new Endereco(null, LocalDateTime.now(), null, null, logradouro, numero, complemento,  bairro, cidade, estado, cep, uf);
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public String getCep() {
        return cep;
    }

    public String getUf() {
        return uf;
    }
}
