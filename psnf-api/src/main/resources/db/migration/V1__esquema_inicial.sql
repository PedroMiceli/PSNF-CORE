-- ---------------------------------------------------------------------------------------------
-- V1: esquema inicial do PSNF, como estava antes do Flyway.
--
-- Gerado com pg_dump a partir de um banco criado pelo Hibernate (ddl-auto=update) + os antigos
-- scripts db/busca-anuncios.sql, db/ajustes-enums.sql e db/indices.sql, que agora vivem aqui.
--
-- Bancos que já existiam antes do Flyway NÃO executam este arquivo: com
-- spring.flyway.baseline-on-migrate=true eles são marcados como "já na versão 1" e seguem a
-- partir da V2. Banco vazio executa tudo do zero.
--
-- NUNCA altere uma migração já aplicada (o Flyway confere o checksum e recusa iniciar):
-- toda mudança de esquema é um arquivo novo, V<n>__descricao.sql.
-- Restrições CHECK de enum: um valor novo no enum exige uma migração atualizando a restrição.
-- ---------------------------------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
CREATE FUNCTION public.psnf_unaccent(texto text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE
    AS $$ SELECT public.unaccent('public.unaccent'::regdictionary, texto) $$;
CREATE TABLE public.anuncio (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    ativo boolean NOT NULL,
    descricao text,
    altura_cm numeric(10,2),
    certificacoes text,
    cest character varying(9),
    comprimento_cm numeric(10,2),
    condicao character varying(20),
    garantia_meses integer,
    garantia_responsavel character varying(20),
    gtin character varying(14),
    largura_cm numeric(10,2),
    marca character varying(120),
    modelo character varying(120),
    ncm character varying(10),
    origem integer,
    peso_bruto_kg numeric(12,3),
    peso_liquido_kg numeric(12,3),
    quantidade_por_embalagem integer,
    sku character varying(60),
    unidade_comercial character varying(6),
    titulo character varying(255) NOT NULL,
    categoria_id uuid NOT NULL,
    vendedor_id uuid NOT NULL,
    busca tsvector GENERATED ALWAYS AS ((setweight(to_tsvector('portuguese'::regconfig, public.psnf_unaccent((COALESCE(titulo, ''::character varying))::text)), 'A'::"char") || setweight(to_tsvector('portuguese'::regconfig, public.psnf_unaccent(COALESCE(descricao, ''::text))), 'B'::"char"))) STORED,
    CONSTRAINT anuncio_condicao_check CHECK (((condicao)::text = ANY ((ARRAY['NOVO'::character varying, 'USADO'::character varying, 'RECONDICIONADO'::character varying])::text[]))),
    CONSTRAINT anuncio_garantia_responsavel_check CHECK (((garantia_responsavel)::text = ANY ((ARRAY['FABRICANTE'::character varying, 'FORNECEDOR'::character varying])::text[])))
);
CREATE TABLE public.avaliacao (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    nota integer NOT NULL,
    texto character varying(250),
    anuncio_id uuid NOT NULL,
    autor_id uuid NOT NULL
);
CREATE TABLE public.avaliacao_imagem (
    avaliacao_id uuid NOT NULL,
    caminho character varying(255) NOT NULL,
    posicao integer NOT NULL,
    CONSTRAINT avaliacao_imagem_posicao_check CHECK ((posicao >= 0))
);
CREATE TABLE public.caracteristica_anuncio (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    nome character varying(60) NOT NULL,
    posicao integer NOT NULL,
    valor character varying(300) NOT NULL,
    anuncio_id uuid NOT NULL
);
CREATE TABLE public.categoria (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    nome character varying(255) NOT NULL
);
CREATE TABLE public.endereco (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    bairro character varying(255),
    cep character varying(255),
    cidade character varying(255),
    complemento character varying(255),
    logradouro character varying(255),
    numero character varying(255),
    uf character varying(2),
    usuario_id uuid NOT NULL
);
CREATE TABLE public.entrega (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    codigo_rastreio character varying(100),
    descricao text,
    ordem_de_pedido_id uuid NOT NULL
);
CREATE TABLE public.imagem_anuncio (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    caminho character varying(255) NOT NULL,
    principal boolean NOT NULL,
    anuncio_id uuid NOT NULL
);
CREATE TABLE public.item_pedido (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    altura_cm numeric(10,2),
    certificacoes text,
    cest character varying(9),
    comprimento_cm numeric(10,2),
    condicao character varying(20),
    garantia_meses integer,
    garantia_responsavel character varying(20),
    gtin character varying(14),
    largura_cm numeric(10,2),
    marca character varying(120),
    modelo character varying(120),
    ncm character varying(10),
    origem integer,
    peso_bruto_kg numeric(12,3),
    peso_liquido_kg numeric(12,3),
    quantidade_por_embalagem integer,
    sku character varying(60),
    unidade_comercial character varying(6),
    quantidade integer NOT NULL,
    titulo_anuncio character varying(255),
    valor_unidade numeric(19,2) NOT NULL,
    variacao character varying(255),
    anuncio_id uuid NOT NULL,
    ordem_de_pedido_id uuid NOT NULL,
    CONSTRAINT item_pedido_condicao_check CHECK (((condicao)::text = ANY ((ARRAY['NOVO'::character varying, 'USADO'::character varying, 'RECONDICIONADO'::character varying])::text[]))),
    CONSTRAINT item_pedido_garantia_responsavel_check CHECK (((garantia_responsavel)::text = ANY ((ARRAY['FABRICANTE'::character varying, 'FORNECEDOR'::character varying])::text[])))
);
CREATE TABLE public.mensagem_incidente (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    imagem character varying(255),
    mensagem text NOT NULL,
    ordem_de_pedido_id uuid NOT NULL,
    usuario_criador_id uuid NOT NULL
);
CREATE TABLE public.mensagem_negociacao (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    ordem_id uuid,
    texto text NOT NULL,
    tipo character varying(20) NOT NULL,
    anuncio_id uuid,
    autor_id uuid NOT NULL,
    negociacao_id uuid NOT NULL
);
CREATE TABLE public.negociacao (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    ultima_mensagem_em timestamp(6) without time zone,
    anuncio_id uuid NOT NULL,
    comprador_id uuid NOT NULL,
    vendedor_id uuid NOT NULL
);
CREATE TABLE public.negociacao_anuncio (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    adicionado_por_id uuid NOT NULL,
    anuncio_id uuid NOT NULL,
    negociacao_id uuid NOT NULL
);
CREATE TABLE public.nota_fiscal (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    caminho character varying(255) NOT NULL,
    ordem_de_pedido_id uuid NOT NULL
);
CREATE TABLE public.ordem_de_pedido (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    atualizada_em timestamp(6) without time zone,
    codigo character varying(255) NOT NULL,
    incidentes_lidos_comprador_em timestamp(6) without time zone,
    incidentes_lidos_vendedor_em timestamp(6) without time zone,
    previsao_de_entrega date,
    status character varying(255) NOT NULL,
    comprador_id uuid NOT NULL,
    negociacao_id uuid,
    vendedor_id uuid NOT NULL
);
CREATE TABLE public.pagamento (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    agencia character varying(255),
    boleto character varying(255),
    chave_pix character varying(255),
    codigo_banco character varying(255),
    comprovante character varying(255),
    conta character varying(255),
    data_comprovante timestamp(6) without time zone,
    data_referencia date,
    nome_referencia character varying(255),
    tipo_pagamento character varying(255) NOT NULL,
    valor numeric(19,2) NOT NULL,
    ordem_de_pedido_id uuid NOT NULL,
    CONSTRAINT pagamento_tipo_pagamento_check CHECK (((tipo_pagamento)::text = ANY ((ARRAY['PIX'::character varying, 'BOLETO'::character varying, 'TRANSFERENCIA'::character varying])::text[])))
);
CREATE TABLE public.usuario (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    imagem character varying(255),
    nome character varying(255) NOT NULL,
    secret2fa character varying(255),
    senha character varying(255) NOT NULL,
    telefone character varying(255)
);
CREATE TABLE public.valor_anuncio (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    quantidade_minima integer NOT NULL,
    valor_unidade numeric(19,2) NOT NULL,
    anuncio_id uuid NOT NULL
);
CREATE TABLE public.variacao_anuncio (
    id uuid NOT NULL,
    data_alteracao timestamp(6) without time zone,
    data_cadastro timestamp(6) without time zone NOT NULL,
    data_excluido timestamp(6) without time zone,
    gtin character varying(14),
    sku character varying(60),
    variacao character varying(255) NOT NULL,
    anuncio_id uuid NOT NULL
);
ALTER TABLE ONLY public.anuncio
    ADD CONSTRAINT anuncio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.avaliacao_imagem
    ADD CONSTRAINT avaliacao_imagem_pkey PRIMARY KEY (avaliacao_id, posicao);
ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT avaliacao_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.caracteristica_anuncio
    ADD CONSTRAINT caracteristica_anuncio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.endereco
    ADD CONSTRAINT endereco_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.entrega
    ADD CONSTRAINT entrega_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.imagem_anuncio
    ADD CONSTRAINT imagem_anuncio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.item_pedido
    ADD CONSTRAINT item_pedido_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.mensagem_incidente
    ADD CONSTRAINT mensagem_incidente_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.mensagem_negociacao
    ADD CONSTRAINT mensagem_negociacao_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.negociacao_anuncio
    ADD CONSTRAINT negociacao_anuncio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.negociacao
    ADD CONSTRAINT negociacao_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.nota_fiscal
    ADD CONSTRAINT nota_fiscal_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.ordem_de_pedido
    ADD CONSTRAINT ordem_de_pedido_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uk5171l57faosmj8myawaucatdw UNIQUE (email);
ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT uk_avaliacao_anuncio_autor UNIQUE (anuncio_id, autor_id);
ALTER TABLE ONLY public.negociacao_anuncio
    ADD CONSTRAINT uk_negociacao_anuncio UNIQUE (negociacao_id, anuncio_id);
ALTER TABLE ONLY public.negociacao
    ADD CONSTRAINT uk_negociacao_anuncio_comprador UNIQUE (anuncio_id, comprador_id);
ALTER TABLE ONLY public.entrega
    ADD CONSTRAINT uki4at32dt0895fyxw7s68ot3g2 UNIQUE (ordem_de_pedido_id);
ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT ukprx5elpv558ah8pk8x18u56yc UNIQUE (nome);
ALTER TABLE ONLY public.ordem_de_pedido
    ADD CONSTRAINT ukqb00t6rorp98yx6myky04yupt UNIQUE (codigo);
ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.valor_anuncio
    ADD CONSTRAINT valor_anuncio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.variacao_anuncio
    ADD CONSTRAINT variacao_anuncio_pkey PRIMARY KEY (id);
CREATE INDEX idx_anuncio_ativo_categoria ON public.anuncio USING btree (categoria_id, data_cadastro DESC) WHERE ativo;
CREATE INDEX idx_anuncio_ativo_recentes ON public.anuncio USING btree (data_cadastro DESC) WHERE ativo;
CREATE INDEX idx_anuncio_busca ON public.anuncio USING gin (busca);
CREATE INDEX idx_anuncio_titulo_trgm ON public.anuncio USING gin (public.psnf_unaccent(lower((titulo)::text)) public.gin_trgm_ops);
CREATE INDEX idx_anuncio_vendedor ON public.anuncio USING btree (vendedor_id);
CREATE INDEX idx_avaliacao_anuncio ON public.avaliacao USING btree (anuncio_id);
CREATE INDEX idx_avaliacao_imagem ON public.avaliacao_imagem USING btree (avaliacao_id);
CREATE INDEX idx_caracteristica_anuncio ON public.caracteristica_anuncio USING btree (anuncio_id);
CREATE INDEX idx_endereco_usuario ON public.endereco USING btree (usuario_id);
CREATE INDEX idx_imagem_anuncio ON public.imagem_anuncio USING btree (anuncio_id);
CREATE INDEX idx_incidente_ordem_data ON public.mensagem_incidente USING btree (ordem_de_pedido_id, data_cadastro);
CREATE INDEX idx_item_pedido_anuncio ON public.item_pedido USING btree (anuncio_id);
CREATE INDEX idx_item_pedido_ordem ON public.item_pedido USING btree (ordem_de_pedido_id);
CREATE INDEX idx_msg_negociacao_data ON public.mensagem_negociacao USING btree (negociacao_id, data_cadastro);
CREATE INDEX idx_negociacao_comprador ON public.negociacao USING btree (comprador_id);
CREATE INDEX idx_negociacao_vendedor ON public.negociacao USING btree (vendedor_id);
CREATE INDEX idx_nota_fiscal_ordem ON public.nota_fiscal USING btree (ordem_de_pedido_id);
CREATE INDEX idx_ordem_comprador ON public.ordem_de_pedido USING btree (comprador_id);
CREATE INDEX idx_ordem_negociacao ON public.ordem_de_pedido USING btree (negociacao_id);
CREATE INDEX idx_ordem_vendedor ON public.ordem_de_pedido USING btree (vendedor_id);
CREATE INDEX idx_pagamento_ordem ON public.pagamento USING btree (ordem_de_pedido_id);
CREATE INDEX idx_valor_anuncio ON public.valor_anuncio USING btree (anuncio_id);
CREATE INDEX idx_variacao_anuncio ON public.variacao_anuncio USING btree (anuncio_id);
ALTER TABLE ONLY public.negociacao_anuncio
    ADD CONSTRAINT fk1h9v5agafuu1k7nf120oksthg FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.mensagem_negociacao
    ADD CONSTRAINT fk2qmnmfrf8dklpujwt91pvbw19 FOREIGN KEY (negociacao_id) REFERENCES public.negociacao(id);
ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT fk3r9ts44rfl2xuoggyeem9wmx7 FOREIGN KEY (autor_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.variacao_anuncio
    ADD CONSTRAINT fk538oe3uob8nu1qop324rfa74 FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.item_pedido
    ADD CONSTRAINT fk56r022p0orhcf5lrswbfk8jvb FOREIGN KEY (ordem_de_pedido_id) REFERENCES public.ordem_de_pedido(id);
ALTER TABLE ONLY public.negociacao
    ADD CONSTRAINT fk6wo3ejwhhfifl5lv0sj9l3fa3 FOREIGN KEY (comprador_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.entrega
    ADD CONSTRAINT fk8gsx7e5jebrxa71x5u4spsdpf FOREIGN KEY (ordem_de_pedido_id) REFERENCES public.ordem_de_pedido(id);
ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT fk8umglg269ibwg4mdkwfomf4tt FOREIGN KEY (ordem_de_pedido_id) REFERENCES public.ordem_de_pedido(id);
ALTER TABLE ONLY public.mensagem_incidente
    ADD CONSTRAINT fk8w9g5mxvyxva4xxd6o59frg7n FOREIGN KEY (ordem_de_pedido_id) REFERENCES public.ordem_de_pedido(id);
ALTER TABLE ONLY public.item_pedido
    ADD CONSTRAINT fk94etvm1ykm9yq674401ieafcw FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT fk95273a403810gkya54v02y8lu FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.negociacao
    ADD CONSTRAINT fkb5fvsgblburyof9b6atqmpxtb FOREIGN KEY (vendedor_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.avaliacao_imagem
    ADD CONSTRAINT fkb8my22s2j6xxxcig42qap6mr5 FOREIGN KEY (avaliacao_id) REFERENCES public.avaliacao(id);
ALTER TABLE ONLY public.nota_fiscal
    ADD CONSTRAINT fkc9ffdd0nldqe0irj5oschjmvs FOREIGN KEY (ordem_de_pedido_id) REFERENCES public.ordem_de_pedido(id);
ALTER TABLE ONLY public.ordem_de_pedido
    ADD CONSTRAINT fkd5xlhq5q4u1g3fguduks9srux FOREIGN KEY (comprador_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.ordem_de_pedido
    ADD CONSTRAINT fkdx87s4aoe6h33wbssn5v87mr5 FOREIGN KEY (negociacao_id) REFERENCES public.negociacao(id);
ALTER TABLE ONLY public.valor_anuncio
    ADD CONSTRAINT fke4jcj2l44029vjucoc46iofhn FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.endereco
    ADD CONSTRAINT fkekdpb8k6gmp3lllla9d1qgmxk FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.anuncio
    ADD CONSTRAINT fkelr0ysxay10e7n7n0lpw7jmxl FOREIGN KEY (categoria_id) REFERENCES public.categoria(id);
ALTER TABLE ONLY public.mensagem_incidente
    ADD CONSTRAINT fkgndtny659nfmb9814hn0q9aon FOREIGN KEY (usuario_criador_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.mensagem_negociacao
    ADD CONSTRAINT fkgprim4hxngswotxlxbk8m73il FOREIGN KEY (autor_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.ordem_de_pedido
    ADD CONSTRAINT fkh4wwunfqvycwfj05pgrdlw9v0 FOREIGN KEY (vendedor_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.imagem_anuncio
    ADD CONSTRAINT fkhc0qdfa8fvbcjk7920pgkqvvp FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.negociacao
    ADD CONSTRAINT fkixk1bqsshwfvv03agma424q47 FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.negociacao_anuncio
    ADD CONSTRAINT fkjf4n8i45eh6ug9ukxif4vka7q FOREIGN KEY (adicionado_por_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.caracteristica_anuncio
    ADD CONSTRAINT fkm6wpnep9ysu6slkullk8fsmic FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.negociacao_anuncio
    ADD CONSTRAINT fknh6bomt50p1tncut5dfnc9v25 FOREIGN KEY (negociacao_id) REFERENCES public.negociacao(id);
ALTER TABLE ONLY public.mensagem_negociacao
    ADD CONSTRAINT fkr1v7h2shn0rtg9hcma4qxldff FOREIGN KEY (anuncio_id) REFERENCES public.anuncio(id);
ALTER TABLE ONLY public.anuncio
    ADD CONSTRAINT fksiolx24ndtlse8vtyadewxvmu FOREIGN KEY (vendedor_id) REFERENCES public.usuario(id);
