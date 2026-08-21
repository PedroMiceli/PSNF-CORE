package com.psnf.api.psnf.anuncio.application.service;

import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import jakarta.transaction.Transactional;

public interface AnuncioServiceInterface {


    //Atencao corno, o event é obrigatório, pois o objeto so e salvo de verdade no postgres quando a funcao acaba, se jogar direto aqui o salvamento no elastic... o elastic salva antes do postgres (se der erro no postgres ele ja vai ter salvo no elastic).
    @Transactional
    Anuncio salvar(Anuncio anuncio);
}
