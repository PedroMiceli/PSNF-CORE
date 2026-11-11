-- ---------------------------------------------------------------------------------------------
-- V8: o comprador retira o pedido no fornecedor (sem envio, portanto sem código de rastreio).
-- O vendedor marca ou desmarca a qualquer momento (o combinado pode mudar depois da compra).
-- ---------------------------------------------------------------------------------------------

ALTER TABLE entrega ADD COLUMN retirada_no_local boolean NOT NULL DEFAULT false;
