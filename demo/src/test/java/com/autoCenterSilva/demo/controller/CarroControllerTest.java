package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autoCenterSilva.demo.dto.response.carro.CarroCreateResponse;
import com.autoCenterSilva.demo.exception.RegraNegocioException;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.service.CarrosService;
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

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarroController.class)
class CarroControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CarrosService carrosService;

    @Test
    @DisplayName("POST /criar com dados válidos retorna 201 e a mensagem de criação")
    void deveRetornar201_quandoCarroForValido() throws Exception {

        String corpo = "{\"marca\":\"Fiat\",\"modelo\":\"Argo\",\"anoFabricacao\":2023,\"versaoAutomovel\":\"1.3\"}";
        given(carrosService.salvarCarro(any()))
                .willReturn(new CarroCreateResponse(1L, "Carro do modelo: Argo adicionado com sucesso!"));

        ResultActions resposta = mvc.perform(post("/api/carro/criar").contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mensage").value("Carro do modelo: Argo adicionado com sucesso!"));
        then(carrosService).should().salvarCarro(argThat(r ->
                r.getMarca().equals("Fiat") && r.getAnoFabricacao() == 2023 && r.getVersaoAutomovel().equals("1.3")));
    }

    static Stream<Arguments> carrosInvalidos() {
        return Stream.of(
                Arguments.of("{\"modelo\":\"Argo\",\"anoFabricacao\":2023,\"versaoAutomovel\":\"1.3\"}", "marca"),
                Arguments.of("{\"marca\":\"Fiat\",\"anoFabricacao\":2023,\"versaoAutomovel\":\"1.3\"}", "modelo"),
                Arguments.of("{\"marca\":\"Fiat\",\"modelo\":\"Argo\",\"versaoAutomovel\":\"1.3\"}", "anoFabricacao"),
                Arguments.of("{\"marca\":\"Fiat\",\"modelo\":\"Argo\",\"anoFabricacao\":2023,\"versaoAutomovel\":\" \"}", "versaoAutomovel")
        );
    }

    @ParameterizedTest(name = "campo inválido: {1}")
    @MethodSource("carrosInvalidos")
    @DisplayName("POST /criar com DTO inválido retorna 400 e não chama o service")
    void deveRetornar400_quandoCarroTiverCampoObrigatorioAusente(String corpo, String campoInvalido) throws Exception {

        ResultActions resposta = mvc.perform(post("/api/carro/criar").contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(startsWith(campoInvalido + ":")));
        then(carrosService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /{carroId}/variacoes/{id} retorna 200 e vincula o carro à variação")
    void deveRetornar200_quandoVinculoForCriado() throws Exception {

        ResultActions resposta = mvc.perform(post("/api/carro/1/variacoes/2"));

        resposta.andExpect(status().isOk());
        then(carrosService).should().vincularCarroVariacao(1L, 2L);
    }

    @Test
    @DisplayName("POST /{carroId}/variacoes/{id} com carro inexistente retorna 404")
    void deveRetornar404_quandoVincularCarroInexistente() throws Exception {

        willThrow(new ResourceNotFoundException("Carro com ID 9 não encontrado"))
                .given(carrosService).vincularCarroVariacao(9L, 2L);

        ResultActions resposta = mvc.perform(post("/api/carro/9/variacoes/2"));

        resposta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Carro com ID 9 não encontrado"));
    }

    @Test
    @DisplayName("DELETE /remover/{carroId}/variacao/{id} retorna 200 e remove o vínculo")
    void deveRetornar200_quandoVinculoForRemovido() throws Exception {

        ResultActions resposta = mvc.perform(delete("/api/carro/remover/1/variacao/2"));

        resposta.andExpect(status().isOk());
        then(carrosService).should().excluirCarroVariacao(1L, 2L);
    }

    @Test
    @DisplayName("DELETE /excluir/{id} sem vínculos retorna 204")
    void deveRetornar204_quandoCarroForExcluido() throws Exception {

        ResultActions resposta = mvc.perform(delete("/api/carro/excluir/1"));

        resposta.andExpect(status().isNoContent());
        then(carrosService).should().excluirCarro(1L);
    }

    @Test
    @DisplayName("DELETE /excluir/{id} de carro vinculado retorna 400")
    void deveRetornar400_quandoExcluirCarroVinculadoAProduto() throws Exception {

        willThrow(new RegraNegocioException("Carro está vinculado a um produto! Não é possível excluir!"))
                .given(carrosService).excluirCarro(1L);

        ResultActions resposta = mvc.perform(delete("/api/carro/excluir/1"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Carro está vinculado a um produto! Não é possível excluir!"));
    }

    @Test
    @DisplayName("DELETE /excluir/{id} com id não numérico retorna 400")
    void deveRetornar400_quandoIdDoCarroNaoForNumerico() throws Exception {

        ResultActions resposta = mvc.perform(delete("/api/carro/excluir/xyz"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro 'carroId' possui formato inválido"));
        then(carrosService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("GET /listar/veiculos retorna 200 com os carros cadastrados")
    void deveRetornarCarrosCadastrados_quandoListarVeiculos() throws Exception {

        given(carrosService.buscarCarros()).willReturn(List.of(new CarroBuscaResponse(1L, "Fiat", "Argo", 2023, "1.3")));

        ResultActions resposta = mvc.perform(get("/api/carro/listar/veiculos"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].marca").value("Fiat"))
                .andExpect(jsonPath("$[0].anoFabricacao").value(2023));
    }

    @Test
    @DisplayName("GET /listar/veiculos sem carros retorna lista vazia")
    void deveRetornarListaVazia_quandoNaoHouverCarros() throws Exception {

        given(carrosService.buscarCarros()).willReturn(List.of());

        ResultActions resposta = mvc.perform(get("/api/carro/listar/veiculos"));

        resposta.andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}
