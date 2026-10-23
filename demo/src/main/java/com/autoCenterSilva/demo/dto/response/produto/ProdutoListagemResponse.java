package com.autoCenterSilva.demo.dto.response.produto;

import com.autocentersilva.demo.entity.ProdutoVariacao;

import java.math.BigDecimal;

public record ProdutoListagemResponse(
        Long id,
        String nome,
        String marca,
        String modelo,
        String medidaCompleta,
        BigDecimal preco,
        Integer indiceCarga
)
{
    public static ProdutoListagemResponse de(ProdutoVariacao produtoVariacao){
        return new ProdutoListagemResponse(
                produtoVariacao.getProduto().getId(),
                produtoVariacao.getProduto().getNome(),
                produtoVariacao.getProduto().getMarca(),
                produtoVariacao.getProduto().getModelo(),
                produtoVariacao.getMedida(),
                produtoVariacao.getPreco(),
                produtoVariacao.getIndiceCarga()
        );
    }
}
