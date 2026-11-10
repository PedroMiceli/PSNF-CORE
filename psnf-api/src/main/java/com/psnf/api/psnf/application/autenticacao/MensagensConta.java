package com.psnf.api.psnf.application.autenticacao;

import com.psnf.api.psnf.domain.autenticacao.model.Email;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** Textos dos e-mails da conta. Os links apontam para as telas do front (psnf.app.url-front). */
@Component
public class MensagensConta {

    private final String urlFront;

    public MensagensConta(@Value("${psnf.app.url-front:http://localhost:5173}") String urlFront) {
        this.urlFront = urlFront.replaceAll("/+$", "");
    }

    Email confirmacao(Usuario u, String token) {
        String link = urlFront + "/confirmar-email?token=" + token;
        return montar(u, "Confirme seu e-mail no PSNF",
                "Olá, " + u.getNome() + "!\n\nPara ativar sua conta no PSNF, confirme seu e-mail abrindo o link abaixo "
                        + "(vale por 24 horas):\n\n" + link + "\n\nSe você não criou esta conta, ignore esta mensagem.",
                "Para ativar sua conta no PSNF, confirme seu e-mail (o link vale por 24 horas).",
                "Confirmar e-mail", link, "Se você não criou esta conta, ignore esta mensagem.");
    }

    Email redefinicao(Usuario u, String token) {
        String link = urlFront + "/redefinir-senha?token=" + token;
        return montar(u, "Redefinição de senha do PSNF",
                "Olá, " + u.getNome() + "!\n\nRecebemos um pedido para redefinir a sua senha. Abra o link abaixo para "
                        + "escolher uma nova (vale por 1 hora e só pode ser usado uma vez):\n\n" + link
                        + "\n\nSe não foi você, ignore esta mensagem: sua senha continua a mesma.",
                "Recebemos um pedido para redefinir a sua senha. O link vale por 1 hora e só pode ser usado uma vez.",
                "Escolher nova senha", link, "Se não foi você, ignore esta mensagem: sua senha continua a mesma.");
    }

    Email senhaAlterada(Usuario u) {
        String link = urlFront + "/esqueci-senha";
        return montar(u, "Sua senha do PSNF foi alterada",
                "Olá, " + u.getNome() + "!\n\nA senha da sua conta acabou de ser alterada e todas as sessões abertas foram "
                        + "encerradas.\n\nSe não foi você, redefina a senha agora: " + link,
                "A senha da sua conta acabou de ser alterada e todas as sessões abertas foram encerradas.",
                "Não fui eu: redefinir a senha", link, "Se foi você, nada mais precisa ser feito.");
    }

    private static Email montar(Usuario u, String assunto, String texto, String paragrafo, String botao,
                                String link, String rodape) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;color:#1f2937">
                  <h2 style="color:#0f172a">PSNF</h2>
                  <p>Olá, %s!</p>
                  <p>%s</p>
                  <p style="margin:28px 0"><a href="%s" style="background:#2563eb;color:#fff;padding:12px 20px;border-radius:8px;text-decoration:none;font-weight:bold">%s</a></p>
                  <p style="font-size:13px;color:#6b7280">Se o botão não funcionar, copie este endereço no navegador:<br>%s</p>
                  <p style="font-size:13px;color:#6b7280">%s</p>
                </div>""".formatted(HtmlUtils.htmlEscape(u.getNome()), HtmlUtils.htmlEscape(paragrafo), link,
                HtmlUtils.htmlEscape(botao), link, HtmlUtils.htmlEscape(rodape));
        return new Email(u.getEmail(), assunto, texto, html);
    }
}
