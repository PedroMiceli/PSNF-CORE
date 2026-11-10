-- ---------------------------------------------------------------------------------------------
-- V4: auditoria de eventos de segurança (login, logout, senha, sessões).
--
-- Serve para investigar incidentes ("de onde entraram na minha conta?") e para o próprio usuário
-- ver a atividade recente no perfil. Só cresce: a limpeza apaga eventos com mais de 1 ano.
-- usuario_id fica nulo quando o evento não pertence a uma conta (ex.: login com e-mail inexistente).
-- ---------------------------------------------------------------------------------------------

CREATE TABLE evento_auditoria (
    id          uuid          PRIMARY KEY,
    usuario_id  uuid          REFERENCES usuario (id),
    tipo        varchar(40)   NOT NULL,
    detalhe     varchar(500),
    ip          varchar(64),
    user_agent  varchar(255),
    criado_em   timestamp(6)  NOT NULL
);

CREATE INDEX idx_evento_auditoria_usuario ON evento_auditoria (usuario_id, criado_em DESC);
CREATE INDEX idx_evento_auditoria_criado ON evento_auditoria (criado_em);
