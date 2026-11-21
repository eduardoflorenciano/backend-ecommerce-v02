package com.autoCenterSilva.demo.dto.response.produto;

import com.autoCenterSilva.demo.entity.ProdutoVariacao;

import java.math.BigDecimal;

public record ProdutoEstoqueAdminResponse(
        Long idProduto,
        Long idVariacao,
        String nome,
        String marca,
        String modelo,
        String medida,
        BigDecimal preco,
        Integer quantidadeEstoque,
        Integer quantidadeReservada,
        Integer quantidadeDisponivel,
        Boolean ativo
) {

    public static ProdutoEstoqueAdminResponse de(ProdutoVariacao variacao) {

        return new ProdutoEstoqueAdminResponse(
                variacao.getProduto().getId(),
                variacao.getId(),
                variacao.getProduto().getNome(),
                variacao.getProduto().getMarca(),
                variacao.getProduto().getModelo(),
                variacao.getMedida(),
                variacao.getPreco(),
                variacao.getQuantidadeEstoque() != null
                        ? variacao.getQuantidadeEstoque() : 0,
                variacao.getQuantidadeReservada() != null
                        ? variacao.getQuantidadeReservada() : 0,
                variacao.getEstoqueDisponivel(),
                variacao.getProduto().getAtivo()
        );
    }
}
