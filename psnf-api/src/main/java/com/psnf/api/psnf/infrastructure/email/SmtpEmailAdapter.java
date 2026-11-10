package com.psnf.api.psnf.infrastructure.email;

import com.psnf.api.psnf.domain.autenticacao.model.Email;
import com.psnf.api.psnf.domain.autenticacao.port.out.EnvioEmailPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;

class SmtpEmailAdapter implements EnvioEmailPort {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailAdapter.class);
    private final JavaMailSender sender;
    private final String remetente;

    SmtpEmailAdapter(JavaMailSender sender, String remetente) {
        this.sender = sender;
        this.remetente = remetente;
    }

    @Override
    public void enviar(Email email) {
        EnvioAposCommit.executar(() -> {
            try {
                MimeMessage msg = sender.createMimeMessage();
                MimeMessageHelper h = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
                h.setFrom(remetente);
                h.setTo(email.para());
                h.setSubject(email.assunto());
                h.setText(email.texto(), email.html()); // texto puro + HTML (o cliente de e-mail escolhe)
                sender.send(msg);
                logger.info("E-mail enviado: \"{}\" para {}", email.assunto(), email.para());
            } catch (Exception e) {
                logger.error("Falha ao enviar e-mail \"{}\" para {}", email.assunto(), email.para(), e);
            }
        });
    }
}
