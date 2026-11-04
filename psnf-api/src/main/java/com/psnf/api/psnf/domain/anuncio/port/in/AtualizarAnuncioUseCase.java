package com.psnf.api.psnf.domain.anuncio.port.in;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.command.AtualizarAnuncioCommand;

public interface AtualizarAnuncioUseCase {
    Anuncio atualizarAnuncio(AtualizarAnuncioCommand command);
}
