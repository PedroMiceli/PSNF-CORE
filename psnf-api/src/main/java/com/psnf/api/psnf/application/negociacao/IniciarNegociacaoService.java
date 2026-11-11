package com.psnf.api.psnf.application.negociacao;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.port.in.IniciarNegociacaoUseCase;
import com.psnf.api.psnf.domain.negociacao.port.out.NegociacaoRepositoryPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IniciarNegociacaoService implements IniciarNegociacaoUseCase {

    private final NegociacaoRepositoryPort negociacaoRepositoryPort;
    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final DetalhesNegociacaoLoader loader;

    @Override
    @Transactional
    public Resultado iniciar(UUID anuncioId, UUID compradorId) {
        // o mesmo comprador não abre duas conversas sobre o mesmo anúncio: reabre a existente
        Optional<Negociacao> existente = negociacaoRepositoryPort.buscarPorAnuncioEComprador(anuncioId, compradorId);
        if (existente.isPresent()) {
            return new Resultado(loader.carregar(existente.get()), false);
        }
        Usuario comprador = usuarioRepositoryPort.buscarPorId(compradorId); // 404 se o comprador não existir
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(anuncioId);
        if (!anuncio.pertenceA(compradorId)) anuncio.garantirQuePodeVenderPara(comprador.temCnpj());
        String nomeVendedor = usuarioRepositoryPort.buscarPorId(anuncio.getVendedor()).getNome();
        Negociacao nova = negociacaoRepositoryPort.save(Negociacao.iniciar(anuncio, compradorId, nomeVendedor));
        return new Resultado(loader.carregar(nova), true);
    }
}
