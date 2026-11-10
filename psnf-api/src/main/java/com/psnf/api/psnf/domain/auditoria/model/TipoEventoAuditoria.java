package com.psnf.api.psnf.domain.auditoria.model;

/** Eventos de segurança registrados. A descrição aparece para o usuário em "Atividade recente". */
public enum TipoEventoAuditoria {
    CADASTRO("Conta criada"),
    EMAIL_CONFIRMADO("E-mail confirmado"),
    LOGIN("Entrou na conta"),
    LOGIN_FALHOU("Tentativa de login com senha errada"),
    LOGIN_BLOQUEADO("Login bloqueado por excesso de tentativas"),
    LOGOUT("Saiu da conta"),
    LOGOUT_TODOS("Encerrou as sessões em todos os dispositivos"),
    SESSAO_ROUBADA("Sessão encerrada: uso de um token já trocado (possível cópia)"),
    REDEFINICAO_SOLICITADA("Pediu o link para redefinir a senha"),
    SENHA_REDEFINIDA("Senha alterada pelo link de e-mail");

    private final String descricao;

    TipoEventoAuditoria(String descricao) { this.descricao = descricao; }

    public String descricao() { return descricao; }
}
