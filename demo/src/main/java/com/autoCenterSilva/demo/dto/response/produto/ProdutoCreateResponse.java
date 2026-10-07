package com.autoCenterSilva.demo.dto.response.produto;

import com.autoCenterSilva.demo.entity.Produtos;

public record ProdutoCreateResponse(Long id,
                                    String nome,
                                    String marca,
                                    String modelo,
                                    String mensage)
{
    public static ProdutoCreateResponse de(Produtos produtos){
        return new ProdutoCreateResponse(
                produtos.getId(),
                produtos.getNome(),
                produtos.getMarca(),
                produtos.getModelo(), "Produto cadastrado com sucesso!"
        );
    }
}
