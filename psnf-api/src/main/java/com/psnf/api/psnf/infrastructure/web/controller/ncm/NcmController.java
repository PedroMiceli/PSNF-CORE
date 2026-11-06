package com.psnf.api.psnf.infrastructure.web.controller.ncm;

import com.psnf.api.psnf.domain.anuncio.model.Ncm;
import com.psnf.api.psnf.domain.anuncio.port.in.ConsultarNcmUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * Tabela NCM, usada no cadastro do anúncio para escolher a classificação fiscal do produto.
 * Ex.: GET /ncm?busca=parafuso  |  GET /ncm/7318.15.00
 */
@RestController
@RequestMapping("/ncm")
@RequiredArgsConstructor
public class NcmController {

    private static final CacheControl UM_DIA = CacheControl.maxAge(Duration.ofDays(1)).cachePublic();

    private final ConsultarNcmUseCase consultarNcmUseCase;

    @GetMapping
    public ResponseEntity<List<Ncm>> pesquisar(@RequestParam("busca") String busca) {
        return ResponseEntity.ok().cacheControl(UM_DIA).body(consultarNcmUseCase.pesquisar(busca));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Ncm> consultar(@PathVariable String codigo) {
        return ResponseEntity.ok().cacheControl(UM_DIA).body(consultarNcmUseCase.consultar(codigo));
    }
}
