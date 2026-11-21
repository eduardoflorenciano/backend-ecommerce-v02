ALTER TABLE produtos_variacao
    ADD COLUMN quantidade_reservada INTEGER NOT NULL DEFAULT 0;

ALTER TABLE pedido
    ADD COLUMN data_vencimento_reserva TIMESTAMP;

ALTER TABLE produtos_variacao
    ADD CONSTRAINT ck_quantidade_reservada
        CHECK (quantidade_reservada >= 0);

CREATE INDEX idx_pedido_status_vencimento
    ON pedido (status, data_vencimento_reserva);
