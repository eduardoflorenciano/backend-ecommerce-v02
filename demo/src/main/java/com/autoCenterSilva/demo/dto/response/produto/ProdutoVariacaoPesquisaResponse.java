package com.autoCenterSilva.demo.dto.response.produto;

import com.autoCenterSilva.demo.dto.response.carro.CarroPesquisaMedidaResponse;
import com.autoCenterSilva.demo.entity.ProdutoVariacao;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoVariacaoPesquisaResponse(
        Long id,
        String nome,
        String medida,
        Integer largura,
        Integer perfil,
        Integer aro,
        Integer indiceCarga,
        BigDecimal preco,
        Integer quantidadeEstoque,
        List<CarroPesquisaMedidaResponse> carrosCompativeis
) {

    public static ProdutoVariacaoPesquisaResponse de(ProdutoVariacao variacao) {
        List<CarroPesquisaMedidaResponse> carros = variacao.getCarros().stream()
                .map(CarroPesquisaMedidaResponse::de)
                .toList();

        return new ProdutoVariacaoPesquisaResponse(
                variacao.getId(),
                variacao.getProduto().getNome(),
                variacao.getMedida(),
                variacao.getLargura(),
                variacao.getPerfil(),
                variacao.getAro(),
                variacao.getIndiceCarga(),
                variacao.getPreco(),
                variacao.getQuantidadeEstoque(),
                carros
        );
    }
}