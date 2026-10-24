package com.autoCenterSilva.demo.service;

import com.autocentersilva.demo.cotacao.CotacaoMoedaClient;
import com.autocentersilva.demo.cotacao.CotacaoMoedaDTO;
import com.autocentersilva.demo.entity.ProdutoVariacao;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.repository.ProdutoVaricaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CotacaoServiceTest {

    @Mock
    private CotacaoMoedaClient client;

    @Mock
    private ProdutoVaricaoRepository repository;

    @InjectMocks
    private CotacaoService service;

    @Test
    void deveDividirPrecoPelaCotacaoComDuasCasas_quandoProdutoExistir() {

        given(repository.findById(1L)).willReturn(Optional.of(variacaoComPreco("100.00")));
        given(client.obterCotacaoDolar()).willReturn(cotacao("3.00"));


        BigDecimal precoEmDolar = service.converterPrecoProdutoParaDolar(1L);


        assertThat(precoEmDolar).isEqualByComparingTo("33.33");
        assertThat(precoEmDolar.scale()).isEqualTo(2);
    }

    @Test
    void deveLancarNaoEncontradoSemChamarApi_quandoProdutoNaoExistir() {

        given(repository.findById(88L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.converterPrecoProdutoParaDolar(88L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto não encontrado com o ID: 88");
        then(client).shouldHaveNoInteractions();
    }

    @Test
    void devePropagarErro_quandoServicoExternoDeCotacaoFalhar() {

        given(repository.findById(1L)).willReturn(Optional.of(variacaoComPreco("25.00")));
        given(client.obterCotacaoDolar()).willThrow(new IllegalStateException("Serviço de cotação indisponível"));

        assertThatThrownBy(() -> service.converterPrecoProdutoParaDolar(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Serviço de cotação indisponível");
    }

    private static ProdutoVariacao variacaoComPreco(String preco) {
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setPreco(new BigDecimal(preco));
        return variacao;
    }

    private static CotacaoMoedaDTO cotacao(String bid) {
        CotacaoMoedaDTO.DadosMoeda dados = new CotacaoMoedaDTO.DadosMoeda();
        dados.setBid(bid);
        CotacaoMoedaDTO dto = new CotacaoMoedaDTO();
        dto.setUsdBrl(dados);
        return dto;
    }
}
