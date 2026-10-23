package com.autoCenterSilva.demo.controller;

import com.autocentersilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autocentersilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoPedidoResponse;
import com.autocentersilva.demo.exception.RegraNegocioException;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.service.PedidoService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidosController.class)
class PedidosControllerTest {

    private static final String URL_CRIAR = "/api/pedidos/criar";
    private static final String PEDIDO_VALIDO = "{\"clienteId\":3,\"itens\":[{\"produtoVariacaoId\":20,\"quantidade\":2}]}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PedidoService pedidoService;

    @Test
    @DisplayName("POST /criar com pedido válido retorna 201 com total, itens e link do WhatsApp")
    void deveRetornar201ComResumoDoPedido_quandoPedidoForValido() throws Exception {

        ProdutoPedidoResponse item = new ProdutoPedidoResponse("Pneu", "175/70R14", 2, new BigDecimal("100.00"));
        PedidoCreateResponse criado = new PedidoCreateResponse(77L, "FINALIZADO", LocalDateTime.of(2026, 1, 1, 10, 0),
                new BigDecimal("200.00"), "Ana", List.of(item), "https://wa.me/5545?text=x");
        given(pedidoService.salvar(any())).willReturn(criado);

        ResultActions resposta = mvc.perform(post(URL_CRIAR).contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO));

        resposta.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.valorTotal").value(200.00))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.linkWhatsapp").value("https://wa.me/5545?text=x"));
        then(pedidoService).should().salvar(argThat(r ->
                r.getClienteId() == 3L && r.getItens().get(0).getProdutoVariacaoId() == 20L));
    }

    static Stream<Arguments> pedidosInvalidos() {
        return Stream.of(
                Arguments.of("{\"itens\":[{\"produtoVariacaoId\":20,\"quantidade\":2}]}", "clienteId"),
                Arguments.of("{\"clienteId\":3,\"itens\":[]}", "itens"),
                Arguments.of("{\"clienteId\":3,\"itens\":[{\"quantidade\":2}]}", "itens[0].produtoVariacaoId"),
                Arguments.of("{\"clienteId\":3,\"itens\":[{\"produtoVariacaoId\":20,\"quantidade\":0}]}", "itens[0].quantidade")
        );
    }

    @ParameterizedTest(name = "campo inválido: {1}")
    @MethodSource("pedidosInvalidos")
    @DisplayName("POST /criar com DTO inválido retorna 400 e não chama o service")
    void deveRetornar400_quandoPedidoTiverCampoInvalido(String corpo, String campoInvalido) throws Exception {

        ResultActions resposta = mvc.perform(post(URL_CRIAR).contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith(campoInvalido + ":")));
        then(pedidoService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /criar sem corpo retorna 400")
    void deveRetornar400_quandoPedidoNaoTiverCorpo() throws Exception {

        ResultActions resposta = mvc.perform(post(URL_CRIAR).contentType(MediaType.APPLICATION_JSON));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou malformado"));
        then(pedidoService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /criar para cliente inexistente retorna 404")
    void deveRetornar404_quandoClienteDoPedidoNaoExistir() throws Exception {

        given(pedidoService.salvar(any())).willThrow(new ResourceNotFoundException("Cliente com o ID 3 não encontrado"));

        ResultActions resposta = mvc.perform(post(URL_CRIAR).contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO));

        resposta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente com o ID 3 não encontrado"));
    }

    @Test
    @DisplayName("POST /criar com estoque insuficiente retorna 400")
    void deveRetornar400_quandoEstoqueForInsuficiente() throws Exception {

        given(pedidoService.salvar(any())).willThrow(new RegraNegocioException("Estoque insuficiente para a medida 175/70R14. Disponível: 1"));

        ResultActions resposta = mvc.perform(post(URL_CRIAR).contentType(MediaType.APPLICATION_JSON).content(PEDIDO_VALIDO));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para a medida 175/70R14. Disponível: 1"));
    }

    @Test
    @DisplayName("GET /listar retorna 200 com uma linha por item de pedido")
    void deveRetornarItensDePedidos_quandoListarPedidos() throws Exception {

        given(pedidoService.buscarPedidos()).willReturn(List.of(
                new PedidosListagemResponse(8L, "Pneu", new BigDecimal("200.00"), "175/70R14", 2)));

        ResultActions resposta = mvc.perform(get("/api/pedidos/listar"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].nomeProduto").value("Pneu"))
                .andExpect(jsonPath("$[0].quantidade").value(2));
    }
}
