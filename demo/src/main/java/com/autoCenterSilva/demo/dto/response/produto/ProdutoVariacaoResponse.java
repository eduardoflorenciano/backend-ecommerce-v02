package com.autoCenterSilva.demo.dto.response.produto;

import com.autoCenterSilva.demo.entity.ProdutoVariacao;

public record ProdutoVariacaoResponse
        (Long idVariacao, Long idProduto, String produtoNome, String mensagem)
{

    public static ProdutoVariacaoResponse de(ProdutoVariacao produtoVariacao){
        return new ProdutoVariacaoResponse(
                produtoVariacao.getId(),
                produtoVariacao.getProduto().getId(),
                produtoVariacao.getProduto().getNome(),
                "Produto: " + produtoVariacao.getProduto().getNome() + " adicionado características com sucesso!"
        );
    }
}
