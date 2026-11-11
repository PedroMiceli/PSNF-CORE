package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Detalhes da entrega informados pelo vendedor (1:1 com a ordem de pedido).
 * Todos opcionais: uma descrição livre (transportadora, horário, instruções...), o código de
 * envio/rastreio e se o comprador retira o pedido no fornecedor (retirada no local). Na retirada
 * não há envio, então não há código de rastreio: a descrição guarda endereço, dia e horário.
 */
public class Entrega extends BaseDomain {

    public static final int TAMANHO_DESCRICAO = 2000;
    public static final int TAMANHO_CODIGO = 100;

    private String descricao;
    private String codigoRastreio;
    private boolean retiradaNoLocal;

    private Entrega(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                    String descricao, String codigoRastreio, boolean retiradaNoLocal) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.descricao = descricao;
        this.codigoRastreio = codigoRastreio;
        this.retiradaNoLocal = retiradaNoLocal;
    }

    /** @return null quando os dois campos vierem vazios (não há o que guardar) */
    static Entrega criar(String descricao, String codigoRastreio, boolean retiradaNoLocal) {
        Entrega e = new Entrega(null, LocalDateTime.now(), null, null, null, null, false);
        return e.atualizar(descricao, codigoRastreio, retiradaNoLocal) ? e : null;
    }

    public static Entrega restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                    String descricao, String codigoRastreio, boolean retiradaNoLocal) {
        return new Entrega(id, dataCadastro, dataAlteracao, dataExcluido, descricao, codigoRastreio, retiradaNoLocal);
    }

    /**
     * Retirada no local descarta o código de rastreio (não há envio).
     * @return false se ficou tudo vazio (sem descrição, sem código e sem retirada)
     */
    boolean atualizar(String descricao, String codigoRastreio, boolean retiradaNoLocal) {
        this.retiradaNoLocal = retiradaNoLocal;
        this.descricao = texto(descricao, TAMANHO_DESCRICAO, "A descrição da entrega");
        this.codigoRastreio = retiradaNoLocal ? null : texto(codigoRastreio, TAMANHO_CODIGO, "O código de envio");
        return !vazia();
    }

    public boolean vazia() {
        return descricao == null && codigoRastreio == null && !retiradaNoLocal;
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
    public boolean isRetiradaNoLocal() { return retiradaNoLocal; }
}
