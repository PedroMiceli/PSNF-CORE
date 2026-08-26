package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncio;

public interface SalvarAnuncioUseCase {
    Anuncio salvarAnuncio(CadastrarAnuncio anuncio);
}
