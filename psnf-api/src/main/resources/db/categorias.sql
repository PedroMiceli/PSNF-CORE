-- ---------------------------------------------------------------------------------------------
-- Categorias fixas da plataforma.
--
-- Compradores e vendedores NÃO criam categorias: a API só oferece leitura (GET /categorias).
-- Para incluir uma categoria, adicione-a nesta lista: na próxima inicialização ela é inserida.
-- Roda a cada inicialização, depois do Hibernate criar as tabelas, e é idempotente: só insere as
-- categorias que ainda não existem (comparando o nome sem diferenciar maiúsculas/minúsculas).
-- Remover um nome daqui NÃO apaga a categoria do banco (pode haver anúncios usando-a).
-- ---------------------------------------------------------------------------------------------

INSERT INTO categoria (id, nome, data_cadastro)
SELECT gen_random_uuid(), nova.nome, now()
FROM (VALUES
    ('Alimentos e Bebidas'),
    ('Embalagens'),
    ('Limpeza e Higiene'),
    ('Material de Escritório'),
    ('Papelaria'),
    ('Informática'),
    ('Eletrônicos'),
    ('Eletrodomésticos'),
    ('Celulares e Acessórios'),
    ('Ferramentas'),
    ('Construção'),
    ('Elétrica'),
    ('Hidráulica'),
    ('Iluminação'),
    ('Móveis'),
    ('Casa e Decoração'),
    ('Cama, Mesa e Banho'),
    ('Utilidades Domésticas'),
    ('Roupas'),
    ('Calçados'),
    ('Moda Íntima'),
    ('Bolsas e Acessórios'),
    ('Uniformes e EPIs'),
    ('Beleza e Perfumaria'),
    ('Saúde'),
    ('Esportes e Fitness'),
    ('Brinquedos'),
    ('Bebês'),
    ('Pet Shop'),
    ('Automotivo'),
    ('Agro e Jardinagem'),
    ('Industrial'),
    ('Logística e Armazenagem'),
    ('Festas e Eventos')
) AS nova(nome)
WHERE NOT EXISTS (SELECT 1 FROM categoria c WHERE lower(c.nome) = lower(nova.nome));
