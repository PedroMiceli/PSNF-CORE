package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.infrastructure.persistence.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class OrdemDePedido extends BaseDomain {

    private String codigo;
    private LocalDate previsaoDeEntrega;


    private Usuario comprador;

    private Usuario vendedor;

    private List<Pagamento> pagamentos = new ArrayList<>();

    private List<MensagemIncidente> mensagensIncidente = new ArrayList<>();

    private List<NotaFiscal> notasFiscais = new ArrayList<>();

    public OrdemDePedido(String codigo, LocalDate previsaoDeEntrega, Usuario comprador, Usuario vendedor, List<Pagamento> pagamentos, List<MensagemIncidente> mensagensIncidente, List<NotaFiscal> notasFiscais) {
        this.codigo = codigo;
        this.previsaoDeEntrega = previsaoDeEntrega;
        this.comprador = comprador;
        this.vendedor = vendedor;
        this.pagamentos = pagamentos;
        this.mensagensIncidente = mensagensIncidente;
        this.notasFiscais = notasFiscais;
    }

    public OrdemDePedido(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String codigo, LocalDate previsaoDeEntrega, Usuario comprador, Usuario vendedor, List<Pagamento> pagamentos, List<MensagemIncidente> mensagensIncidente, List<NotaFiscal> notasFiscais) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.codigo = codigo;
        this.previsaoDeEntrega = previsaoDeEntrega;
        this.comprador = comprador;
        this.vendedor = vendedor;
        this.pagamentos = pagamentos;
        this.mensagensIncidente = mensagensIncidente;
        this.notasFiscais = notasFiscais;
    }


    public String getCodigo() {
        return codigo;
    }

    public LocalDate getPrevisaoDeEntrega() {
        return previsaoDeEntrega;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public List<MensagemIncidente> getMensagensIncidente() {
        return mensagensIncidente;
    }

    public List<NotaFiscal> getNotasFiscais() {
        return notasFiscais;
    }
}
