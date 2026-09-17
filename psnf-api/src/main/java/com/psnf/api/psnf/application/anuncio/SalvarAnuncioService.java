package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.application.anuncio.event.AnuncioSalvoEvent;
import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.domain.anuncio.port.in.SalvarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommandMapper;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static co.elastic.clients.elasticsearch.connector.ConnectorFieldType.List;

@Service
@RequiredArgsConstructor
public class SalvarAnuncioService implements SalvarAnuncioUseCase {

    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public Anuncio salvarAnuncio(CadastrarAnuncioCommand anuncio) {
        UUID idUsuarioLogado = UUID.fromString("1b713366-3be1-4485-b7b3-f2c61b70224a");

        Anuncio anuncioDomain = Anuncio.criar(
                LocalDateTime.now(),
                null,
                null,
                anuncio.titulo(),
                anuncio.descricao(),
                true,
                anuncio.categoriaId(),
                idUsuarioLogado,
                CadastrarAnuncioCommandMapper.toValores(anuncio.valores()),
                CadastrarAnuncioCommandMapper.toImagens(anuncio.imagens()),
                CadastrarAnuncioCommandMapper.toVariacoes(anuncio.variacoes())
        );

        Anuncio anuncioSalvo = anuncioRepositoryPort.save(anuncioDomain);

        eventPublisher.publishEvent(new AnuncioSalvoEvent(anuncioSalvo.getId()));

        return anuncioSalvo;
    }

}
