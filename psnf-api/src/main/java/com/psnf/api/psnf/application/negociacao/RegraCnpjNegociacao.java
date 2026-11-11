package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Comprador sem CNPJ não abre nem usa o chat de uma negociação que tem anúncio "somente para empresas"
 * (ex.: tinha CNPJ quando começou a conversa e depois o removeu). O vendedor não é afetado.
 */
@Component
@RequiredArgsConstructor
public class RegraCnpjNegociacao {

    private final AnuncioRepositoryPort anuncios;
    private final UsuarioRepositoryPort usuarios;

    public void garantirAcesso(Negociacao n, UUID usuarioId) {
        if (!usuarioId.equals(n.getCompradorId())) return;
        var daNegociacao = anuncios.buscarPorIds(n.getAnuncioIds()).values();
        if (daNegociacao.stream().noneMatch(Anuncio::isExigeCnpj)) return; // nada exclusivo: sem consulta extra
        boolean temCnpj = usuarios.buscarPorId(usuarioId).temCnpj();
        daNegociacao.stream().filter(Anuncio::isExigeCnpj).findFirst().ifPresent(a -> a.garantirQuePodeVenderPara(temCnpj));
    }
}
