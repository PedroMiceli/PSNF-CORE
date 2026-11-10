package com.psnf.api.psnf.infrastructure.email;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SOMENTE DESENVOLVIMENTO: e-mails que teriam sido enviados (sem SMTP), para testes automáticos pegarem
 * os links de confirmação e de redefinição. Exige login como qualquer rota. Desligado com
 * PSNF_USUARIOS_TESTE=false (psnf.email.caixa-dev).
 */
@RestController
@RequestMapping("/dev/emails")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "psnf.email.caixa-dev", havingValue = "true")
public class CaixaDevEmailsController {

    private final CaixaDevEmails caixa;

    @GetMapping
    public List<CaixaDevEmails.EmailGuardado> listar(@RequestParam(required = false) String para) {
        return caixa.para(para);
    }
}
