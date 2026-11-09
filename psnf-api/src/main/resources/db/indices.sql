-- ---------------------------------------------------------------------------------------------
-- Índices das chaves estrangeiras e das consultas mais frequentes.
-- O PostgreSQL NÃO cria índice para chave estrangeira sozinho: sem eles, buscar "as ordens do
-- comprador X" ou "as mensagens da negociação Y" varre a tabela inteira (piora com o volume).
-- Executado a cada inicialização, depois do Hibernate (comandos idempotentes).
-- ---------------------------------------------------------------------------------------------

-- ordem de pedido: listas de compras/vendas, ordens entre comprador e vendedor, novidades
CREATE INDEX IF NOT EXISTS idx_ordem_comprador        ON ordem_de_pedido (comprador_id);
CREATE INDEX IF NOT EXISTS idx_ordem_vendedor         ON ordem_de_pedido (vendedor_id);
CREATE INDEX IF NOT EXISTS idx_ordem_negociacao       ON ordem_de_pedido (negociacao_id);
CREATE INDEX IF NOT EXISTS idx_item_pedido_ordem      ON item_pedido (ordem_de_pedido_id);
CREATE INDEX IF NOT EXISTS idx_item_pedido_anuncio    ON item_pedido (anuncio_id);
CREATE INDEX IF NOT EXISTS idx_pagamento_ordem        ON pagamento (ordem_de_pedido_id);
CREATE INDEX IF NOT EXISTS idx_nota_fiscal_ordem      ON nota_fiscal (ordem_de_pedido_id);
CREATE INDEX IF NOT EXISTS idx_incidente_ordem_data   ON mensagem_incidente (ordem_de_pedido_id, data_cadastro);

-- negociação (chat): mensagens novas em ordem de data (chat por delta)
CREATE INDEX IF NOT EXISTS idx_msg_negociacao_data    ON mensagem_negociacao (negociacao_id, data_cadastro);

-- anúncio e filhos (carregados juntos na vitrine e no detalhe)
CREATE INDEX IF NOT EXISTS idx_anuncio_vendedor       ON anuncio (vendedor_id);
CREATE INDEX IF NOT EXISTS idx_imagem_anuncio         ON imagem_anuncio (anuncio_id);
CREATE INDEX IF NOT EXISTS idx_valor_anuncio          ON valor_anuncio (anuncio_id);
CREATE INDEX IF NOT EXISTS idx_variacao_anuncio       ON variacao_anuncio (anuncio_id);
CREATE INDEX IF NOT EXISTS idx_caracteristica_anuncio ON caracteristica_anuncio (anuncio_id);

-- outros
CREATE INDEX IF NOT EXISTS idx_endereco_usuario       ON endereco (usuario_id);
CREATE INDEX IF NOT EXISTS idx_avaliacao_imagem       ON avaliacao_imagem (avaliacao_id);
