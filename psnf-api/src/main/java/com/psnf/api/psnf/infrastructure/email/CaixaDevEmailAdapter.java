package com.psnf.api.psnf.infrastructure.email;

import com.psnf.api.psnf.domain.autenticacao.model.Email;
import com.psnf.api.psnf.domain.autenticacao.port.out.EnvioEmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sem SMTP: o e-mail vai para o log (com o link, para o desenvolvedor clicar) e para a caixa de dev. */
class CaixaDevEmailAdapter implements EnvioEmailPort {

    private static final Logger logger = LoggerFactory.getLogger(CaixaDevEmailAdapter.class);
    private final CaixaDevEmails caixa;

    CaixaDevEmailAdapter(CaixaDevEmails caixa) {
        this.caixa = caixa;
    }

    @Override
    public void enviar(Email email) {
        EnvioAposCommit.executar(() -> {
            caixa.guardar(email);
            logger.info("E-mail (sem SMTP, não enviado) para {}: \"{}\"\n{}", email.para(), email.assunto(), email.texto());
        });
    }
}
