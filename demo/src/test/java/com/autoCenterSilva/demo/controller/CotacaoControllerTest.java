package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.service.CotacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CotacaoController.class)
class CotacaoControllerTest {

    private static final String URL = "/cotacao/produto/{id}/converter-para-reais";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CotacaoService cotacaoService;

    @Test
    @DisplayName("GET de produto existente retorna 200 com o preço convertido")
    void deveRetornarPrecoConvertido_quandoProdutoExistir() throws Exception {

        given(cotacaoService.converterPrecoProdutoParaDolar(7L)).willReturn(new BigDecimal("12.50"));

        ResultActions resposta = mvc.perform(get(URL, 7));

        resposta.andExpect(status().isOk()).andExpect(content().string("12.50"));
    }

    @Test
    @DisplayName("GET de produto inexistente retorna 404")
    void deveRetornar404_quandoProdutoNaoExistir() throws Exception {

        given(cotacaoService.converterPrecoProdutoParaDolar(99L))
                .willThrow(new ResourceNotFoundException("Produto não encontrado com o ID: 99"));

        ResultActions resposta = mvc.perform(get(URL, 99));

        resposta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto não encontrado com o ID: 99"));
    }

    @Test
    @DisplayName("GET com falha no serviço externo retorna 500 sem vazar detalhes")
    void deveRetornar500SemDetalhesInternos_quandoServicoDeCotacaoFalhar() throws Exception {

        given(cotacaoService.converterPrecoProdutoParaDolar(7L)).willThrow(new IllegalStateException("timeout na API"));

        ResultActions resposta = mvc.perform(get(URL, 7));

        resposta.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value(GlobalExceptionHandler.ERRO_INTERNO))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado no sistema"));
    }
}
