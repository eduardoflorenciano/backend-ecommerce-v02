ALTER TABLE pedido
    ADD COLUMN data_cancelamento TIMESTAMP;

ALTER TABLE pedido
    ADD COLUMN motivo_cancelamento VARCHAR(500);
