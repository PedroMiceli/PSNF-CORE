package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

import java.util.List;
import java.util.UUID;

/**
 * Vitrine: anúncios ativos, opcionalmente filtrados por categoria (consulta direta no banco).
 */
public interface ListarAnunciosAtivosUseCase {

    List<Anuncio> listarAtivos(UUID categoriaId);
}
