-- ---------------------------------------------------------------------------------------------
-- V5: CNPJ do usuário e anúncios vendidos somente para quem tem CNPJ.
--
-- usuario.cnpj: opcional (pessoas físicas também compram), sem pontuação, maiúsculo (o CNPJ
-- alfanumérico de 2026 tem letras). Uma conta por CNPJ: o índice único ignora quem não tem CNPJ.
--
-- anuncio.exige_cnpj: o vendedor decide se vende só para empresas. Padrão true, inclusive para os
-- anúncios que já existem; quem não tem CNPJ não vê esses anúncios.
-- ---------------------------------------------------------------------------------------------

ALTER TABLE usuario ADD COLUMN cnpj varchar(14);
CREATE UNIQUE INDEX uk_usuario_cnpj ON usuario (cnpj) WHERE cnpj IS NOT NULL;

ALTER TABLE anuncio ADD COLUMN exige_cnpj boolean NOT NULL DEFAULT true;
-- vitrine de quem não tem CNPJ: só os anúncios liberados para pessoa física
CREATE INDEX idx_anuncio_ativo_sem_cnpj ON anuncio (data_cadastro DESC) WHERE ativo AND NOT exige_cnpj;
