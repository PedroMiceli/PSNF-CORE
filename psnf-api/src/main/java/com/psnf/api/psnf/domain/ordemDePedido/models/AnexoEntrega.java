package com.psnf.api.psnf.domain.ordemDePedido.models;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Arquivo anexado à entrega (foto da carga saindo, do comprador retirando, comprovante da transportadora...),
 * com quem enviou e quando: serve de prova de que o pedido saiu ou foi retirado.
 */
public class AnexoEntrega extends BaseDomain {

    private final String caminho;
    private final UUID enviadoPor;

    private AnexoEntrega(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                         String caminho, UUID enviadoPor) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.caminho = caminho;
        this.enviadoPor = enviadoPor;
    }

    /** @param caminho arquivo já enviado ao storage (POST /arquivos devolve "/arquivos/...") */
    static AnexoEntrega criar(String caminho, UUID enviadoPor) {
        if (caminho == null || !caminho.startsWith("/arquivos/") || caminho.length() > 255) {
            throw new IllegalArgumentException("Envie o arquivo primeiro (POST /arquivos) e informe o caminho devolvido");
        }
        return new AnexoEntrega(null, LocalDateTime.now(), null, null, caminho, enviadoPor);
    }

    public static AnexoEntrega restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido,
                                         String caminho, UUID enviadoPor) {
        return new AnexoEntrega(id, dataCadastro, dataAlteracao, dataExcluido, caminho, enviadoPor);
    }

    public String getCaminho() { return caminho; }
    public UUID getEnviadoPor() { return enviadoPor; }
}
