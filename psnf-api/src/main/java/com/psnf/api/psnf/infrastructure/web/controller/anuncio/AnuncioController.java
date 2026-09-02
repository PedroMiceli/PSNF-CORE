package com.psnf.api.psnf.infrastructure.web.controller.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.SalvarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarAnuncioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.AnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.AnuncioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.mapper.AnuncioRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final SalvarAnuncioUseCase salvarAnuncioUseCase;

    @PostMapping
    public ResponseEntity<AnuncioResponse> salvar(
            @RequestBody @Valid AnuncioRequest request
    ) {
        CadastrarAnuncioCommand command = AnuncioRequestMapper.toCommand(request);

        Anuncio anuncioSalvo = salvarAnuncioUseCase.salvarAnuncio(command);

        AnuncioResponse response = AnuncioResponse.from(anuncioSalvo);

        return ResponseEntity
                .created(URI.create("/anuncios/" + response.id()))
                .body(response);
    }
}
