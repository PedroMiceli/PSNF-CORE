package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;

public interface SalvarAnuncioUseCase {

    Anuncio salvarAnuncio(SalvarAnuncioCommand anuncio);
}
