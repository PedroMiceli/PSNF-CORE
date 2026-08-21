package com.psnf.api.psnf.anuncio.application;

import com.psnf.api.psnf.anuncio.application.DTO.request.AnuncioRequest;
import com.psnf.api.psnf.anuncio.application.DTO.AnuncioResponse;
import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import com.psnf.api.psnf.anuncio.application.service.AnuncioServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnuncioApplication implements AnuncioApplicationInterface{

    @Autowired
    private AnuncioServiceInterface anuncioServiceInterface;

    public AnuncioResponse salvarAnuncio(AnuncioRequest anuncioRequest) {
        Anuncio anuncio = Anunc
        anuncioServiceInterface.salvar()
    }
}
