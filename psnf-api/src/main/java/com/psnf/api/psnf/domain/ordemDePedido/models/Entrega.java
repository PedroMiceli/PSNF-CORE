package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Detalhes da entrega informados pelo vendedor (1:1 com a ordem de pedido).
 * Os dois campos são opcionais: uma descrição livre (transportadora, horário, instruções...)
 * e o código de envio/rastreio.
 */
public class Entrega extends BaseDomain {

    public static final int TAMANHO_DESCRICAO = 2000;
    public static final int TAMANHO_CODIGO = 100;

    private String descricao;
    private String codigoRastreio;

    private Entrega(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                    String descricao, String codigoRastreio) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.descricao = descricao;
        this.codigoRastreio = codigoRastreio;
    }

    /** @return null quando os dois campos vierem vazios (não há o que guardar) */
    static Entrega criar(String descricao, String codigoRastreio) {
        Entrega e = new Entrega(null, LocalDateTime.now(), null, null, null, null);
        return e.atualizar(descricao, codigoRastreio) ? e : null;
    }

    public static Entrega restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                    String descricao, String codigoRastreio) {
        return new Entrega(id, dataCadastro, dataAlteracao, dataExcluido, descricao, codigoRastreio);
    }

    /** @return false se os dois campos ficaram vazios */
    boolean atualizar(String descricao, String codigoRastreio) {
        this.descricao = texto(descricao, TAMANHO_DESCRICAO, "A descrição da entrega");
        this.codigoRastreio = texto(codigoRastreio, TAMANHO_CODIGO, "O código de envio");
        return !vazia();
    }

    public boolean vazia() {
        return descricao == null && codigoRastreio == null;
    }

    private static String texto(String valor, int maximo, String campo) {
        if (valor == null || valor.isBlank()) return null;
        String t = valor.strip();
        if (t.length() > maximo) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + maximo + " caracteres");
        }
        return t;
    }

    public String getDescricao() { return descricao; }
    public String getCodigoRastreio() { return codigoRastreio; }
}
