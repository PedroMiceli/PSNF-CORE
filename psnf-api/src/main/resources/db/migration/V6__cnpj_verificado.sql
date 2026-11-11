-- ---------------------------------------------------------------------------------------------
-- V6: dados do CNPJ confirmados na Receita Federal (consulta pela BrasilAPI ao salvar o CNPJ).
--
-- razao_social e cnpj_verificado_em só são preenchidos quando a Receita confirma o CNPJ como ATIVO.
-- CNPJ que a base pública ainda não tem (ex.: alfanumérico recém-emitido) fica salvo, sem verificação.
-- ---------------------------------------------------------------------------------------------

ALTER TABLE usuario ADD COLUMN razao_social varchar(255);
ALTER TABLE usuario ADD COLUMN cnpj_verificado_em timestamp(6);
