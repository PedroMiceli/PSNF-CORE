-- ---------------------------------------------------------------------------------------------
-- Busca de texto dos anúncios no próprio PostgreSQL (substitui o Elasticsearch).
--
-- Executado a cada inicialização da API, DEPOIS que o Hibernate cria/atualiza as tabelas
-- (spring.jpa.defer-datasource-initialization=true). Todos os comandos são idempotentes.
--
--  * unaccent: ignora acentos ("agua" encontra "Água")
--  * dicionário portuguese: reduz as palavras ao radical ("carrinhos" encontra "carrinho")
--  * pg_trgm: tolera erros de digitação ("carinho" encontra "carrinho")
-- ---------------------------------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- unaccent() não é IMMUTABLE e por isso não pode ser usado em índices nem em colunas geradas;
-- esta versão com o dicionário fixo pode.
CREATE OR REPLACE FUNCTION psnf_unaccent(texto text) RETURNS text
    LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT
    AS $$ SELECT public.unaccent('public.unaccent'::regdictionary, texto) $$;

-- Documento de busca calculado e mantido pelo próprio banco a cada INSERT/UPDATE:
-- título com peso A (mais relevante) e descrição com peso B.
ALTER TABLE anuncio ADD COLUMN IF NOT EXISTS busca tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('portuguese', psnf_unaccent(coalesce(titulo, ''))), 'A') ||
    setweight(to_tsvector('portuguese', psnf_unaccent(coalesce(descricao, ''))), 'B')
) STORED;

CREATE INDEX IF NOT EXISTS idx_anuncio_busca ON anuncio USING GIN (busca);
CREATE INDEX IF NOT EXISTS idx_anuncio_titulo_trgm ON anuncio USING GIN (psnf_unaccent(lower(titulo)) gin_trgm_ops);

-- vitrine sem termo: anúncios ativos, mais recentes primeiro (geral e por categoria)
CREATE INDEX IF NOT EXISTS idx_anuncio_ativo_recentes ON anuncio (data_cadastro DESC) WHERE ativo;
CREATE INDEX IF NOT EXISTS idx_anuncio_ativo_categoria ON anuncio (categoria_id, data_cadastro DESC) WHERE ativo;
