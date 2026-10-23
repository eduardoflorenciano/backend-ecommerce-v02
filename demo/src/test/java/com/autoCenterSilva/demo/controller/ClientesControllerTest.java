package com.autoCenterSilva.demo.controller;

import com.autocentersilva.demo.dto.response.cliente.ClienteAtualizaResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteBuscaResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteCreateResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteLoginResponse;
import com.autocentersilva.demo.entity.Cliente;
import com.autocentersilva.demo.entity.PerfilUsuario;
import com.autocentersilva.demo.exception.CredenciaisInvalidasException;
import com.autocentersilva.demo.exception.RegraNegocioException;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.service.ClientesService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientesController.class)
class ClientesControllerTest {

    private static final String CADASTRO_VALIDO =
            "{\"nome\":\"Ana\",\"telefone\":\"45999990000\",\"senha\":\"senha123\",\"confirmarSenha\":\"senha123\"}";
    private static final String LOGIN_VALIDO =
            "{\"nome\":\"Ana\",\"telefone\":\"45999990000\",\"senha\":\"senha123\"}";
    private static final String ATUALIZACAO_VALIDA =
            "{\"nome\":\"Ana\",\"senha\":\"novaSenha1\",\"confirmarSenha\":\"novaSenha1\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ClientesService clientesService;

    @Test
    @DisplayName("POST /criar com dados válidos retorna 201 e o cliente criado")
    void deveRetornar201ComClienteCriado_quandoCadastroForValido() throws Exception {

        given(clientesService.salvarCliente(any()))
                .willReturn(new ClienteCreateResponse(1L, "Ana", "Cliente cadastrado com sucesso!"));

        ResultActions resposta = mvc.perform(post("/api/clientes/criar")
                .contentType(MediaType.APPLICATION_JSON).content(CADASTRO_VALIDO));

        resposta.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.mensagem").value("Cliente cadastrado com sucesso!"));
        then(clientesService).should().salvarCliente(argThat(r ->
                r.getNome().equals("Ana") && r.getTelefone().equals("45999990000")));
    }

    static Stream<Arguments> cadastrosInvalidos() {
        return Stream.of(
                Arguments.of("{\"telefone\":\"45999990000\",\"senha\":\"senha123\",\"confirmarSenha\":\"senha123\"}", "nome"),
                Arguments.of("{\"nome\":\"Ana\",\"senha\":\"senha123\",\"confirmarSenha\":\"senha123\"}", "telefone"),
                Arguments.of("{\"nome\":\"Ana\",\"telefone\":\"45999990000\",\"senha\":\"123\",\"confirmarSenha\":\"senha123\"}", "senha"),
                Arguments.of("{\"nome\":\"Ana\",\"telefone\":\"45999990000\",\"senha\":\"senha123\"}", "confirmarSenha")
        );
    }

    @ParameterizedTest(name = "campo inválido: {1}")
    @MethodSource("cadastrosInvalidos")
    @DisplayName("POST /criar com DTO inválido retorna 400 e não chama o service")
    void deveRetornar400SemChamarService_quandoCadastroTiverCampoInvalido(String corpo, String campoInvalido) throws Exception {

        ResultActions resposta = mvc.perform(post("/api/clientes/criar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value(GlobalExceptionHandler.ERRO_VALIDACAO_CAMPOS))
                .andExpect(jsonPath("$.message").value(startsWith(campoInvalido + ":")));
        then(clientesService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /criar com corpo malformado retorna 400")
    void deveRetornar400_quandoCorpoDoCadastroForJsonMalformado() throws Exception {

        ResultActions resposta = mvc.perform(post("/api/clientes/criar")
                .contentType(MediaType.APPLICATION_JSON).content("{nome:"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(GlobalExceptionHandler.ERRO_REQUISICAO_INVALIDA));
        then(clientesService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /criar com nome duplicado retorna 400 com a mensagem da regra de negócio")
    void deveRetornar400ComMensagem_quandoServiceRejeitarCadastroDuplicado() throws Exception {

        given(clientesService.salvarCliente(any())).willThrow(new RegraNegocioException("Nome já existe"));

        ResultActions resposta = mvc.perform(post("/api/clientes/criar")
                .contentType(MediaType.APPLICATION_JSON).content(CADASTRO_VALIDO));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(GlobalExceptionHandler.ERRO_REGRA_NEGOCIO))
                .andExpect(jsonPath("$.message").value("Nome já existe"));
    }

    @Test
    @DisplayName("POST /login com credenciais válidas retorna 200 e mensagem de boas-vindas")
    void deveRetornar200ComBoasVindas_quandoLoginForValido() throws Exception {

        given(clientesService.validarLogin(any()))
                .willReturn(new ClienteLoginResponse(1L, "Ana", "Bem-vindo: Ana - Login realizado com sucesso!"));

        ResultActions resposta = mvc.perform(post("/api/clientes/login")
                .contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mensagem").value("Bem-vindo: Ana - Login realizado com sucesso!"));
    }

    @Test
    @DisplayName("POST /login com senha curta retorna 400 e não chama o service")
    void deveRetornar400_quandoSenhaDoLoginTiverMenosDeSeisCaracteres() throws Exception {

        String corpo = "{\"nome\":\"Ana\",\"telefone\":\"45999990000\",\"senha\":\"123\"}";

        ResultActions resposta = mvc.perform(post("/api/clientes/login")
                .contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("senha:")));
        then(clientesService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /login com credenciais incorretas retorna 401")
    void deveRetornar401_quandoCredenciaisDoLoginForemIncorretas() throws Exception {

        given(clientesService.validarLogin(any())).willThrow(new CredenciaisInvalidasException("Credenciais incorretas"));

        ResultActions resposta = mvc.perform(post("/api/clientes/login")
                .contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO));

        resposta.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciais incorretas"));
    }

    @Test
    @DisplayName("PATCH /atualizar com dados válidos retorna 200 e mensagem de sucesso")
    void deveRetornar200_quandoAtualizacaoDeSenhaForValida() throws Exception {

        given(clientesService.atualizarSenha(eq(2L), any()))
                .willReturn(new ClienteAtualizaResponse("Usuário: Ana senha alterada com sucesso!"));

        ResultActions resposta = mvc.perform(patch("/api/clientes/atualizar/2")
                .contentType(MediaType.APPLICATION_JSON).content(ATUALIZACAO_VALIDA));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Usuário: Ana senha alterada com sucesso!"));
    }

    @Test
    @DisplayName("PATCH /atualizar sem confirmação de senha retorna 400")
    void deveRetornar400_quandoAtualizacaoNaoTiverConfirmacaoDeSenha() throws Exception {

        String corpo = "{\"nome\":\"Ana\",\"senha\":\"novaSenha1\"}";

        ResultActions resposta = mvc.perform(patch("/api/clientes/atualizar/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("confirmarSenha:")));
        then(clientesService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("PATCH /atualizar com nome divergente retorna 401")
    void deveRetornar401_quandoNomeInformadoNaAtualizacaoForDiferente() throws Exception {

        given(clientesService.atualizarSenha(eq(2L), any())).willThrow(new CredenciaisInvalidasException("Nome está incorreto"));

        ResultActions resposta = mvc.perform(patch("/api/clientes/atualizar/2")
                .contentType(MediaType.APPLICATION_JSON).content(ATUALIZACAO_VALIDA));

        resposta.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Nome está incorreto"));
    }

    @Test
    @DisplayName("GET /busca/{id} existente retorna 200 sem expor a senha")
    void deveRetornar200SemSenha_quandoClienteExistir() throws Exception {

        given(clientesService.findById(4L))
                .willReturn(new ClienteBuscaResponse(4L, "Ana", "45999990000", "Usuário encontrado com sucesso!"));

        ResultActions resposta = mvc.perform(get("/api/clientes/busca/4"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.telefone").value("45999990000"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("GET /busca/{id} inexistente retorna 404")
    void deveRetornar404_quandoClienteNaoExistir() throws Exception {

        given(clientesService.findById(404L)).willThrow(new ResourceNotFoundException("Usuário com ID 404 não encontrado"));

        ResultActions resposta = mvc.perform(get("/api/clientes/busca/404"));

        resposta.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(GlobalExceptionHandler.ERRO_RECURSO_NAO_ENCONTRADO))
                .andExpect(jsonPath("$.message").value("Usuário com ID 404 não encontrado"));
    }

    @Test
    @DisplayName("GET /busca/{id} com id não numérico retorna 400")
    void deveRetornar400SemChamarService_quandoIdNaoForNumerico() throws Exception {

        ResultActions resposta = mvc.perform(get("/api/clientes/busca/abc"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro 'id' possui formato inválido"));
        then(clientesService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("DELETE /desativar/{id} retorna 204 e delega a desativação")
    void deveRetornar204_quandoClienteForDesativado() throws Exception {

        ResultActions resposta = mvc.perform(delete("/api/clientes/desativar/3"));

        resposta.andExpect(status().isNoContent());
        then(clientesService).should().desativarCliente(3L);
    }

    @Test
    @DisplayName("DELETE /desativar/{id} com pedido em aberto retorna 400")
    void deveRetornar400_quandoClienteTiverPedidoEmAberto() throws Exception {

        willThrow(new RegraNegocioException("Cliente está com um pedido de venda em aberto!"))
                .given(clientesService).desativarCliente(3L);

        ResultActions resposta = mvc.perform(delete("/api/clientes/desativar/3"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cliente está com um pedido de venda em aberto!"));
    }

    @Test
    @DisplayName("GET /usuarios sem parâmetro filtra pelo perfil CLIENTE")
    void deveListarClientesAtivos_quandoPerfilNaoForInformado() throws Exception {

        given(clientesService.buscarDisponiveis(PerfilUsuario.CLIENTE)).willReturn(List.of(cliente(1L, "Ana")));

        ResultActions resposta = mvc.perform(get("/api/clientes/usuarios"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    @DisplayName("GET /usuarios/desativados sem parâmetro filtra pelo perfil DESATIVADO")
    void deveListarClientesDesativados_quandoPerfilNaoForInformado() throws Exception {

        given(clientesService.buscarDisponiveis(PerfilUsuario.DESATIVADO)).willReturn(List.of(cliente(2L, "Bia")));

        ResultActions resposta = mvc.perform(get("/api/clientes/usuarios/desativados"));

        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @DisplayName("GET /usuarios com perfil inexistente retorna 400")
    void deveRetornar400_quandoPerfilInformadoNaoExistir() throws Exception {

        ResultActions resposta = mvc.perform(get("/api/clientes/usuarios").param("perfilUsuario", "INVALIDO"));

        resposta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro 'perfilUsuario' possui formato inválido"));
        then(clientesService).shouldHaveNoInteractions();
    }

    private static Cliente cliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setTelefone("4599999000" + id);
        cliente.setSenha("segredo");
        return cliente;
    }
}
