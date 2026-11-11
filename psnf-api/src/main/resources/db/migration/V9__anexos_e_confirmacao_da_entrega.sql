-- ---------------------------------------------------------------------------------------------
-- V9: arquivos da entrega (prova de saída da carga ou de retirada) e quem confirmou a entrega.
--
-- anexo_entrega: fotos/PDFs que o vendedor anexa à entrega (envio ou retirada no local), com quem
-- enviou e quando. Ficam ligados à ordem (não à linha de "entrega"), para não sumirem se o vendedor
-- limpar a descrição ou o código de rastreio. Depois da confirmação da entrega, não podem ser removidos.
--
-- ordem_de_pedido.entrega_confirmada_por_id/_em: quem apertou "confirmar entrega/retirada" e quando
-- (na retirada no local, o vendedor também pode confirmar).
-- ---------------------------------------------------------------------------------------------

CREATE TABLE anexo_entrega (
    id                  uuid          PRIMARY KEY,
    ordem_de_pedido_id  uuid          NOT NULL REFERENCES ordem_de_pedido (id),
    caminho             varchar(255)  NOT NULL,
    enviado_por_id      uuid          NOT NULL REFERENCES usuario (id),
    data_cadastro       timestamp(6)  NOT NULL,
    data_alteracao      timestamp(6),
    data_excluido       timestamp(6)
);
CREATE INDEX idx_anexo_entrega_ordem ON anexo_entrega (ordem_de_pedido_id);

ALTER TABLE ordem_de_pedido ADD COLUMN entrega_confirmada_por_id uuid REFERENCES usuario (id);
ALTER TABLE ordem_de_pedido ADD COLUMN entrega_confirmada_em timestamp(6);
