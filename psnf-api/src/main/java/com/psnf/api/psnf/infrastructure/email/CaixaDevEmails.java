package com.psnf.api.psnf.infrastructure.email;

import com.psnf.api.psnf.domain.autenticacao.model.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Últimos e-mails "enviados" sem SMTP, para testes e desenvolvimento (GET /dev/emails).
 * Ativa junto com os usuários de teste: em produção os dois ficam desligados (PSNF_USUARIOS_TESTE=false),
 * porque os links de redefinição de senha ficariam expostos.
 */
@Component
public class CaixaDevEmails {

    public record EmailGuardado(LocalDateTime enviadoEm, String para, String assunto, String texto) {
    }

    private static final int MAXIMO = 100;
    private final boolean ativa;
    private final Deque<EmailGuardado> emails = new ArrayDeque<>();

    public CaixaDevEmails(@Value("${psnf.email.caixa-dev:true}") boolean ativa) {
        this.ativa = ativa;
    }

    public boolean ativa() { return ativa; }

    synchronized void guardar(Email e) {
        if (!ativa) return;
        emails.addFirst(new EmailGuardado(LocalDateTime.now(), e.para(), e.assunto(), e.texto()));
        while (emails.size() > MAXIMO) emails.removeLast();
    }

    /** mais recentes primeiro */
    public synchronized List<EmailGuardado> para(String destinatario) {
        return emails.stream().filter(e -> destinatario == null || e.para().equalsIgnoreCase(destinatario)).toList();
    }
}
