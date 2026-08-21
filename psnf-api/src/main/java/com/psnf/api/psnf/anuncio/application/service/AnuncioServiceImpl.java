package com.psnf.api.psnf.anuncio.application.service;

import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import com.psnf.api.psnf.anuncio.adapters.outbound.repositories.AnuncioJPARepository;
import com.psnf.api.psnf.anuncio.event.AnuncioSalvoEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AnuncioServiceImpl implements AnuncioServiceInterface {

    @Autowired
    private AnuncioJPARepository anuncioJPARepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    //Atencao corno, o event é obrigatório, pois o objeto so e salvo de verdade no postgres quando a funcao acaba, se jogar direto aqui o salvamento no elastic... o elastic salva antes do postgres (se der erro no postgres ele ja vai ter salvo no elastic).
    @Transactional
    @Override
    public Anuncio salvar(Anuncio anuncio){
//        Anuncio salvo = anuncioJPARepository.save(anuncio);
//        eventPublisher.publishEvent(new AnuncioSalvoEvent(salvo.getId()));
//        return salvo;
    }

}
