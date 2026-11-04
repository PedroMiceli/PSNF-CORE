package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.AnuncioResumo;

import java.util.List;
import java.util.UUID;

public interface PesquisarAnunciosUseCase {

    List<AnuncioResumo> pesquisar(String termo, UUID categoriaId);
}
