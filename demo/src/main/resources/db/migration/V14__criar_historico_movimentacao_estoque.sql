CREATE TABLE historico_movimentacao_estoque (
    id BIGSERIAL PRIMARY KEY,
    produto_variacao_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    quantidade INTEGER NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    data_movimentacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT fk_movimentacao_variacao
            FOREIGN KEY (produto_variacao_id)
            REFERENCES produtos_variacao(id),

        CONSTRAINT chk_movimentacao_tipo
            CHECK (tipo IN ('ENTRADA', 'SAIDA')),

        CONSTRAINT chk_movimentacao_quantidade
            CHECK (quantidade > 0)
);