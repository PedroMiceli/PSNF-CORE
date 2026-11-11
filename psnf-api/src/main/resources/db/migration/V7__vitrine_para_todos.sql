-- ---------------------------------------------------------------------------------------------
-- V7: todos veem todos os anúncios; o CNPJ só conta na hora de negociar. O índice da vitrine
-- "sem CNPJ" (V5) deixou de ser usado.
-- ---------------------------------------------------------------------------------------------

DROP INDEX IF EXISTS idx_anuncio_ativo_sem_cnpj;
