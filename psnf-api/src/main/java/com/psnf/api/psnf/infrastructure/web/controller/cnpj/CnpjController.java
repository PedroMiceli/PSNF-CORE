package com.psnf.api.psnf.infrastructure.web.controller.cnpj;

import com.psnf.api.psnf.domain.usuario.model.EmpresaConsultada;
import com.psnf.api.psnf.domain.usuario.port.in.ConsultarCnpjUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Dados públicos do CNPJ na Receita (razão social, situação, endereço), para preencher o cadastro.
 * Pública porque o cadastro acontece antes do login; limitada por IP (regra "consultas-externas").
 * Ex.: GET /cnpj/19.131.243/0001-97
 */
@RestController
@RequestMapping("/cnpj")
@RequiredArgsConstructor
public class CnpjController {

    private final ConsultarCnpjUseCase consultarCnpjUseCase;

    @GetMapping("/{cnpj}")
    public ResponseEntity<EmpresaConsultada> consultar(@PathVariable String cnpj) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(consultarCnpjUseCase.consultar(cnpj));
    }
}
