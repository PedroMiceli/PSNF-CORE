package com.psnf.api.psnf.infrastructure.web.controller.arquivo;

import com.psnf.api.psnf.domain.arquivo.model.Arquivo;
import com.psnf.api.psnf.domain.arquivo.model.ConteudoArquivo;
import com.psnf.api.psnf.domain.arquivo.port.in.BaixarArquivoUseCase;
import com.psnf.api.psnf.domain.arquivo.port.in.EnviarArquivoUseCase;
import com.psnf.api.psnf.domain.arquivo.port.in.command.EnviarArquivoCommand;
import com.psnf.api.psnf.infrastructure.web.controller.arquivo.dto.ArquivoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * Upload em duas etapas:
 *  1. POST /arquivos (multipart, campo "arquivo") -> devolve o "caminho" (/arquivos/{uuid}.{ext})
 *  2. o caminho é usado no cadastro do anúncio, da nota fiscal, da mensagem, do usuário...
 */
@RestController
@RequestMapping("/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final EnviarArquivoUseCase enviarArquivoUseCase;
    private final BaixarArquivoUseCase baixarArquivoUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArquivoResponse> enviar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        Arquivo salvo = enviarArquivoUseCase.enviar(new EnviarArquivoCommand(arquivo.getOriginalFilename(), arquivo.getBytes()));
        return ResponseEntity
                .created(URI.create(salvo.caminho()))
                .body(ArquivoResponse.from(salvo));
    }

    @GetMapping("/{nome}")
    public ResponseEntity<InputStreamResource> baixar(@PathVariable String nome) {
        ConteudoArquivo arquivo = baixarArquivoUseCase.baixar(nome);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.contentType()))
                .contentLength(arquivo.tamanho())
                // o nome é um UUID e o conteúdo nunca muda: pode ficar em cache por bastante tempo
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nome + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(arquivo.conteudo()));
    }
}
