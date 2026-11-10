-- ---------------------------------------------------------------------------------------------
-- V3: confirmação de e-mail e recuperação de senha.
--
-- usuario.email_confirmado_em: nulo = ainda não clicou no link de confirmação (não consegue entrar).
-- Quem já tinha conta antes desta versão é considerado confirmado, para ninguém ficar trancado fora.
--
-- token_usuario: links de uso único enviados por e-mail (confirmação e redefinição de senha).
-- Como nos refresh tokens, só o hash SHA-256 é guardado: quem lê o banco não consegue usar os links.
-- ---------------------------------------------------------------------------------------------

ALTER TABLE usuario ADD COLUMN email_confirmado_em timestamp(6);
UPDATE usuario SET email_confirmado_em = coalesce(data_cadastro, now());

CREATE TABLE token_usuario (
    id          uuid         PRIMARY KEY,
    usuario_id  uuid         NOT NULL REFERENCES usuario (id),
    tipo        varchar(30)  NOT NULL,
    token_hash  varchar(64)  NOT NULL UNIQUE,
    criado_em   timestamp(6) NOT NULL,
    expira_em   timestamp(6) NOT NULL,
    usado_em    timestamp(6)
);

CREATE INDEX idx_token_usuario_usuario_tipo ON token_usuario (usuario_id, tipo);
CREATE INDEX idx_token_usuario_expira ON token_usuario (expira_em);
