package com.autoCenterSilva.demo.controller;

import com.autocentersilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.service.ProdutosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutosController.class)
class ProdutosControllerTest {

    private static final String VARIACAO_VALIDA =
            "{\"largura\":205,\"perfil\":55,\"aro\":16,\"indiceCarga\":91,\"preco\":399.90,\"quantidadeEstoque\":8}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProdutosService produtosService;

    @Test
    @DisplayName("POST /criar com produto válido retorna 201 com nome formatado")
    void deveRetornar201_quandoProdutoForValido() throws Exception {

        String corpo = "{\"marca\":\"Pirelli\",\"modelo\":\"P1\"}";
        given(produtosService.salvarProduto(any()))
                .willReturn(new ProdutoCreateResponse(3L, "Pneu Pirelli P1", "Pirelli", "P1", "Produto cadastrado com sucesso!"));

        ResultActions resposta = mvc.perform(post("/api/produtos/criar").contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Pneu Pirelli P1"));
        then(produtosService).should().salvarProduto(argThat(r -> r.getMarca().equals("Pirelli") && r.getModelo().equals("P1")));
    }

    @Test
    @DisplayName("POST /criar sem marca retorna 400")
    void deveRetornar400_quandoProdutoNaoTiverMarca() throws Exception {

        String corpo = "{\"modelo\":\"P1\"}";

        ResultActions resposta = mvc.perform(post("/api/produtos/criar").contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("marca: Marca é obrigatório"));
        then(produtosService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /variacao/{id} com dados válidos retorna 201")
    void deveRetornar201_quandoVariacaoForValida() throws Exception {

        given(produtosService.salvarVariacao(eq(7L), any()))
                .willReturn(new ProdutoVariacaoResponse(11L, 7L, "Pneu Pirelli P1", "Produto: Pneu Pirelli P1 adicionado características com sucesso!"));

        ResultActions resposta = mvc.perform(post("/api/produtos/variacao/7").contentType(MediaType.APPLICATION_JSON).content(VARIACAO_VALIDA));

        resposta.andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVariacao").value(11))
                .andExpect(jsonPath("$.idProduto").value(7));
    }

    static Stream<Arguments> variacoesInvalidas() {
        return Stream.of(
                Arguments.of("{\"largura\":50,\"perfil\":55,\"aro\":16,\"indiceCarga\":91,\"preco\":399.90,\"quantidadeEstoque\":8}", "largura"),
                Arguments.of("{\"largura\":205,\"perfil\":99,\"aro\":16,\"indiceCarga\":91,\"preco\":399.90,\"quantidadeEstoque\":8}", "perfil"),
                Arguments.of("{\"largura\":205,\"perfil\":55,\"aro\":40,\"indiceCarga\":91,\"preco\":399.90,\"quantidadeEstoque\":8}", "aro"),
                Arguments.of("{\"largura\":205,\"perfil\":55,\"aro\":16,\"indiceCarga\":91,\"preco\":-1,\"quantidadeEstoque\":8}", "preco"),
                Arguments.of("{\"largura\":205,\"perfil\":55,\"aro\":16,\"indiceCarga\":91,\"preco\":399.90,\"quantidadeEstoque\":1}", "quantidadeEstoque")
        );
    }

    @ParameterizedTest(name = "campo inválido: {1}")
    @MethodSource("variacoesInvalidas")
    @DisplayName("POST /variacao/{id} com medida fora dos limites retorna 400")
    void deveRetornar400_quandoVariacaoViolarLimites(String corpo, String campoInvalido) throws Exception {

        ResultActions resposta = mvc.perform(post("/api/produtos/variacao/7").contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith(campoInvalido + ":")));
        then(produtosService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /variacao/{id} de produto inexistente retorna 404")
    void deveRetornar404_quandoProdutoDaVariacaoNaoExistir() throws Exception {

        given(produtosService.salvarVariacao(eq(8L), any())).willThrow(new ResourceNotFoundException("Produto com ID 8 não encontrado"));

        ResultActions resposta = mvc.perform(post("/api/produtos/variacao/8").contentType(MediaType.APPLICATION_JSON).content(VARIACAO_VALIDA));

        resposta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto com ID 8 não encontrado"));
    }

    @Test
    @DisplayName("DELETE /variacoes/{id}/desvincular retorna 200 e desvincula a variação")
    void deveRetornar200_quandoVariacaoForDesvinculada() throws Exception {

        ResultActions resposta = mvc.perform(delete("/api/produtos/variacoes/5/desvincular"));

        resposta.andExpect(status().isOk());
        then(produtosService).should().excluirVariacaoProduto(5L);
    }

    @Test
    @DisplayName("DELETE /variacoes/{id}/desvincular de variação inexistente retorna 404")
    void deveRetornar404_quandoDesvincularVariacaoInexistente() throws Exception {

        willThrow(new ResourceNotFoundException("Variação com ID 5 não encontrada")).given(produtosService).excluirVariacaoProduto(5L);

        ResultActions resposta = mvc.perform(delete("/api/produtos/variacoes/5/desvincular"));

        resposta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Variação com ID 5 não encontrada"));
    }

    @Test
    @DisplayName("GET /listar retorna 200 com as variações de produtos")
    void deveRetornarProdutos_quandoListarProdutos() throws Exception {

        given(produtosService.buscarProdutos()).willReturn(List.of(
                new ProdutoListagemResponse(1L, "Pneu Pirelli P1", "Pirelli", "P1", "205/55R16", new BigDecimal("399.90"), 91)));

        ResultActions resposta = mvc.perform(get("/api/produtos/listar"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].medidaCompleta").value("205/55R16"))
                .andExpect(jsonPath("$[0].preco").value(399.90));
    }

    @Test
    @DisplayName("POST /pesquisar com medida retorna 200 com variações e carros compatíveis")
    void deveRetornarVariacoesCompativeis_quandoPesquisarPorMedida() throws Exception {

        CarroBuscaResponse carro = new CarroBuscaResponse(2L, "Fiat", "Uno", 2015, "1.0");
        given(produtosService.pesquisarPorMedida(any())).willReturn(List.of(new ProdutoVariacaoPesquisaResponse(
                12L, "Pneu", "175/70R14", 175, 70, 14, 88, new BigDecimal("299.90"), 8, List.of(carro))));

        ResultActions resposta = mvc.perform(post("/api/produtos/pesquisar")
                .contentType(MediaType.APPLICATION_JSON).content("{\"medida\":\"175/70\"}"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medida").value("175/70R14"))
                .andExpect(jsonPath("$[0].carrosCompativeis[0].modelo").value("Uno"));
        then(produtosService).should().pesquisarPorMedida(argThat(r -> r.getMedida().equals("175/70")));
    }

    @Test
    @DisplayName("POST /pesquisar com medida em branco retorna 400")
    void deveRetornar400_quandoMedidaPesquisadaEstiverEmBranco() throws Exception {

        ResultActions resposta = mvc.perform(post("/api/produtos/pesquisar")
                .contentType(MediaType.APPLICATION_JSON).content("{\"medida\":\"\"}"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("medida: A medida é obrigatória"));
        then(produtosService).shouldHaveNoInteractions();
    }
}
