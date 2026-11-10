package com.psnf.api.psnf.infrastructure.email;

import com.psnf.api.psnf.domain.autenticacao.port.out.EnvioEmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Qual envio de e-mail usar:
 * <ul>
 *   <li>spring.mail.host configurado (PSNF_SMTP_HOST): SMTP de verdade (SES, SendGrid, Mailgun, Gmail...).</li>
 *   <li>sem SMTP: {@link CaixaDevEmailAdapter}, que só registra no log e guarda as últimas mensagens em
 *       memória (desenvolvimento e testes).</li>
 * </ul>
 */
@Configuration
public class EmailConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);

    @Bean
    EnvioEmailPort envioEmail(ObjectProvider<JavaMailSender> smtp, CaixaDevEmails caixa,
                              @Value("${spring.mail.host:}") String host,
                              @Value("${psnf.email.remetente:PSNF <nao-responda@psnf.com.br>}") String remetente) {
        // spring.mail.host vazio ainda faz o Spring criar o JavaMailSender: decide pelo valor
        JavaMailSender sender = host.isBlank() ? null : smtp.getIfAvailable();
        if (sender != null) {
            logger.info("E-mails: enviados por SMTP ({}), remetente {}", host, remetente);
            return new SmtpEmailAdapter(sender, remetente);
        }
        logger.warn("E-mails: SMTP não configurado (PSNF_SMTP_HOST); as mensagens vão só para o log"
                + (caixa.ativa() ? " e para GET /dev/emails" : "") + ". Configure o SMTP em produção.");
        return new CaixaDevEmailAdapter(caixa);
    }
}
