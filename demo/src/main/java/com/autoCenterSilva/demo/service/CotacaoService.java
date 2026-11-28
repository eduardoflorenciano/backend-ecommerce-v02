package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.cotacao.CotacaoMoedaClient;
import com.autoCenterSilva.demo.cotacao.CotacaoMoedaDTO;
import com.autoCenterSilva.demo.entity.ProdutoVariacao;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.ProdutoVaricaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CotacaoService {

    private final CotacaoMoedaClient client;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;

    public CotacaoService(CotacaoMoedaClient client, ProdutoVaricaoRepository produtoVaricaoRepository) {
        this.client = client;
        this.produtoVaricaoRepository = produtoVaricaoRepository;
    }

    public BigDecimal converterPrecoProdutoParaDolar(Long idProduto) {
        ProdutoVariacao produtoVariacao = produtoVaricaoRepository.findById(idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + idProduto));

        CotacaoMoedaDTO dto = client.obterCotacaoDolar();
        BigDecimal cotacaoDolar = new BigDecimal(dto.getUsdBrl().getBid());

        return produtoVariacao.getPreco().divide(cotacaoDolar, 2, RoundingMode.HALF_UP);
    }
}
