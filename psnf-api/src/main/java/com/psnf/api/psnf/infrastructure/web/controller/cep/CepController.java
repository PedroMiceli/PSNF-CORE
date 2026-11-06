package com.psnf.api.psnf.infrastructure.web.controller.cep;

import com.psnf.api.psnf.domain.usuario.model.EnderecoConsultado;
import com.psnf.api.psnf.domain.usuario.port.in.ConsultarCepUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Busca de endereço pelo CEP, usada para preencher o formulário de endereço.
 * Ex.: GET /cep/01310-100 -> { cep, logradouro, bairro, cidade, uf }
 */
@RestController
@RequestMapping("/cep")
@RequiredArgsConstructor
public class CepController {

    private final ConsultarCepUseCase consultarCepUseCase;

    @GetMapping("/{cep}")
    public ResponseEntity<EnderecoConsultado> consultar(@PathVariable String cep) {
        return ResponseEntity.ok()
                // endereço de um CEP praticamente não muda: o navegador pode reaproveitar por um dia
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .body(consultarCepUseCase.consultar(cep));
    }
}
