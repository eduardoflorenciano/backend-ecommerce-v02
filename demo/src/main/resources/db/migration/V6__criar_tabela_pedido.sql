CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50),
    valor_total NUMERIC(15, 2),
    data_pedido TIMESTAMP,
    cliente_id BIGINT,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
);