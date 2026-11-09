-- ---------------------------------------------------------------------------------------------
-- O Hibernate cria uma restrição CHECK com os valores do enum quando a tabela é criada, mas o
-- ddl-auto=update não a atualiza depois. Sem isso, um valor novo no enum (ex.: um tipo de
-- mensagem novo) seria recusado nos bancos já existentes. A validação fica no domínio.
-- Executado a cada inicialização, depois do Hibernate (comandos idempotentes).
-- ---------------------------------------------------------------------------------------------

ALTER TABLE IF EXISTS mensagem_negociacao DROP CONSTRAINT IF EXISTS mensagem_negociacao_tipo_check;
-- status AGUARDANDO_CONFIRMACAO e ACEITO entraram depois da criação da tabela
ALTER TABLE IF EXISTS ordem_de_pedido DROP CONSTRAINT IF EXISTS ordem_de_pedido_status_check;
