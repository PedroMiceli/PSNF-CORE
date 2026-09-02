package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;

public interface SalvarAnuncioUseCase {
    Anuncio salvarAnuncio(CadastrarAnuncioCommand anuncio);
}
