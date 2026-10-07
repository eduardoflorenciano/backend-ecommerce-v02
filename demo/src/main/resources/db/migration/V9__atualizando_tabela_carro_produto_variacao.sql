DROP TABLE IF EXISTS carro_produto_variacao;

CREATE TABLE carro_produto_variacao (
    carro_id BIGINT NOT NULL REFERENCES carro(id),
    produto_variacao_id BIGINT NOT NULL REFERENCES produtos_variacao(id),
    PRIMARY KEY (carro_id, produto_variacao_id)
);