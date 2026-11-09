package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response;

import com.psnf.api.psnf.domain.ordemDePedido.models.MensagemIncidente;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

/** Ordem com mensagens de incidente novas para o usuário logado (alertas no topo do site). */
public record AlertaMensagemResponse(
        UUID ordemId,
        String codigo,
        /** COMPRADOR ou VENDEDOR: o papel do usuário logado na ordem */
        String papel,
        String outroParticipanteNome,
        int naoLidas,
        String ultimaMensagem,
        LocalDateTime ultimaMensagemEm
) {
    public static AlertaMensagemResponse from(OrdemDePedido o, UUID usuarioLogado) {
        boolean souComprador = o.isComprador(usuarioLogado);
        Usuario outro = souComprador ? o.getVendedor() : o.getComprador();
        MensagemIncidente ultima = o.getMensagensIncidente().stream()
                .filter(m -> !usuarioLogado.equals(m.getUsuarioCriador()))
                .max(Comparator.comparing(MensagemIncidente::getDataCadastro)).orElse(null);
        return new AlertaMensagemResponse(o.getId(), o.getCodigo(), souComprador ? "COMPRADOR" : "VENDEDOR", outro.getNome(),
                o.mensagensIncidenteNaoLidas(usuarioLogado),
                ultima == null ? null : ultima.getMensagem(), ultima == null ? null : ultima.getDataCadastro());
    }
}
