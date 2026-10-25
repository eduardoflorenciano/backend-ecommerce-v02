package com.autoCenterSilva.demo.dto.response.produto;

import com.autoCenterSilva.demo.dto.response.carro.CarroBuscaResponse;
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
        List<CarroBuscaResponse> carrosCompativeis
) {

    public static ProdutoVariacaoPesquisaResponse de(ProdutoVariacao variacao) {
        List<CarroBuscaResponse> carros = variacao.getCarros().stream()
                .map(CarroBuscaResponse::de)
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