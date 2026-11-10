-- ---------------------------------------------------------------------------------------------
-- V2: sessões de login (refresh tokens).
--
-- O access token (JWT, 15 min) não é guardado: qualquer instância da API o valida pela assinatura.
-- O refresh token (30 dias) fica aqui para poder ser revogado (logout, troca de senha, roubo).
-- Guardamos só o hash SHA-256 do token: quem ler o banco não consegue usar as sessões.
--
-- Rotação: cada renovação marca o token como usado e emite outro da mesma "família" (o mesmo login).
-- Se um token já usado for apresentado de novo, alguém o copiou: a família inteira é revogada.
-- ---------------------------------------------------------------------------------------------

CREATE TABLE refresh_token (
    id          uuid         PRIMARY KEY,
    usuario_id  uuid         NOT NULL REFERENCES usuario (id),
    familia     uuid         NOT NULL,
    token_hash  varchar(64)  NOT NULL UNIQUE,
    criado_em   timestamp(6) NOT NULL,
    expira_em   timestamp(6) NOT NULL,
    usado_em    timestamp(6),
    revogado_em timestamp(6),
    ip          varchar(64),
    user_agent  varchar(255)
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);
CREATE INDEX idx_refresh_token_familia ON refresh_token (familia);
CREATE INDEX idx_refresh_token_expira ON refresh_token (expira_em);
