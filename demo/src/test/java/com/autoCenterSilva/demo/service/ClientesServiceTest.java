package com.autoCenterSilva.demo.service;

import com.autocentersilva.demo.dto.request.cliente.ClienteAtualizaRequest;
import com.autocentersilva.demo.dto.request.cliente.ClienteCreatRequest;
import com.autocentersilva.demo.dto.request.cliente.ClienteLoginRequest;
import com.autocentersilva.demo.dto.response.cliente.ClienteAtualizaResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteBuscaResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteCreateResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteLoginResponse;
import com.autocentersilva.demo.entity.Cliente;
import com.autocentersilva.demo.entity.PerfilUsuario;
import com.autocentersilva.demo.exception.CredenciaisInvalidasException;
import com.autocentersilva.demo.exception.RegraNegocioException;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.repository.ClientesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ClientesServiceTest {

    private static final String TELEFONE = "45999990000";

    @Mock
    private ClientesRepository repository;

    @InjectMocks
    private ClientesService service;

    @Nested
    @DisplayName("Consulta de clientes")
    class Consulta {

        @Test
        void deveRetornarTodosOsClientes_quandoListarSemFiltro() {

            Cliente cliente = clienteAtivo();
            given(repository.findAll()).willReturn(List.of(cliente));

            List<Cliente> resultado = service.findAll();

            assertThat(resultado).containsExactly(cliente);
        }

        @Test
        void deveRetornarDadosDoCliente_quandoIdExistir() {

            given(repository.findById(1L)).willReturn(Optional.of(clienteAtivo()));

            ClienteBuscaResponse resposta = service.findById(1L);

            assertThat(resposta.id()).isEqualTo(1L);
            assertThat(resposta.nome()).isEqualTo("Flor");
            assertThat(resposta.telefone()).isEqualTo(TELEFONE);
            assertThat(resposta.message()).isEqualTo("Usuário encontrado com sucesso!");
        }

        @Test
        void deveLancarNaoEncontrado_quandoIdNaoExistir() {

            given(repository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuário com ID 99 não encontrado");
        }

        @Test
        void deveFiltrarPeloPerfilInformado_quandoBuscarDisponiveis() {

            Cliente cliente = clienteAtivo();
            given(repository.findByPerfil(PerfilUsuario.CLIENTE)).willReturn(List.of(cliente));

            List<Cliente> resultado = service.buscarDisponiveis(PerfilUsuario.CLIENTE);

            assertThat(resultado).containsExactly(cliente);
            then(repository).should().findByPerfil(PerfilUsuario.CLIENTE);
        }
    }

    @Nested
    @DisplayName("Cadastro de clientes")
    class Cadastro {

        @Test
        void deveSalvarClienteDesativado_quandoDadosForemValidosEUnicos() {

            ClienteCreatRequest request = cadastro("senha123", "senha123");
            given(repository.existsByNome("Flor")).willReturn(false);
            given(repository.existsByTelefone(TELEFONE)).willReturn(false);
            given(repository.save(any(Cliente.class))).willAnswer(inv -> {
                Cliente salvo = inv.getArgument(0);
                salvo.setId(10L);
                return salvo;
            });

            ClienteCreateResponse resposta = service.salvarCliente(request);

            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            then(repository).should().save(captor.capture());
            Cliente persistido = captor.getValue();
            assertThat(persistido.getPerfil()).isEqualTo(PerfilUsuario.DESATIVADO);
            assertThat(persistido.getNome()).isEqualTo("Flor");
            assertThat(persistido.getTelefone()).isEqualTo(TELEFONE);
            assertThat(persistido.getSenha()).isEqualTo("senha123");
            assertThat(resposta.id()).isEqualTo(10L);
            assertThat(resposta.mensagem()).isEqualTo("Cliente cadastrado com sucesso!");
        }

        @Test
        void deveRecusarSemConsultarBanco_quandoSenhasNaoCoincidirem() {

            ClienteCreatRequest request = cadastro("senha123", "outra123");


            assertThatThrownBy(() -> service.salvarCliente(request))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("As senhas não coincidem");
            then(repository).shouldHaveNoInteractions();
        }

        @Test
        void deveRecusarSemSalvar_quandoNomeJaExistir() {

            ClienteCreatRequest request = cadastro("senha123", "senha123");
            given(repository.existsByNome("Flor")).willReturn(true);

            assertThatThrownBy(() -> service.salvarCliente(request))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Nome de usuário inserido já existe");
            then(repository).should(never()).save(any());
        }

        @Test
        void deveRecusarSemSalvar_quandoTelefoneJaExistir() {

            ClienteCreatRequest request = cadastro("senha123", "senha123");
            given(repository.existsByNome("Flor")).willReturn(false);
            given(repository.existsByTelefone(TELEFONE)).willReturn(true);

            assertThatThrownBy(() -> service.salvarCliente(request))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Telefone inserido já existe");
            then(repository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        void deveAtivarPerfilESalvar_quandoCredenciaisForemValidas() {

            Cliente cliente = clienteDesativado();
            given(repository.findByTelefone(TELEFONE)).willReturn(Optional.of(cliente));
            ClienteLoginRequest request = login("Flor", "senha123");

            ClienteLoginResponse resposta = service.validarLogin(request);

            assertThat(cliente.getPerfil()).isEqualTo(PerfilUsuario.CLIENTE);
            assertThat(resposta.id()).isEqualTo(1L);
            assertThat(resposta.mensagem()).isEqualTo("Bem-vindo: Flor - Login realizado com sucesso!");
            then(repository).should().save(cliente);
        }

        @Test
        void deveLancarCredenciaisInvalidas_quandoTelefoneNaoExistir() {

            given(repository.findByTelefone(TELEFONE)).willReturn(Optional.empty());
            ClienteLoginRequest request = login("Flor", "senha123");

            assertThatThrownBy(() -> service.validarLogin(request))
                    .isInstanceOf(CredenciaisInvalidasException.class)
                    .hasMessage(ClientesService.CREDENCIAIS_INVALIDAS);
        }

        @ParameterizedTest(name = "nome={0}, senha={1}")
        @CsvSource({"Flor, errada123", "Outro, senha123"})
        void deveManterPerfilDesativado_quandoNomeOuSenhaNaoConferirem(String nome, String senha) {

            Cliente cliente = clienteDesativado();
            given(repository.findByTelefone(TELEFONE)).willReturn(Optional.of(cliente));
            ClienteLoginRequest request = login(nome, senha);

            assertThatThrownBy(() -> service.validarLogin(request))
                    .isInstanceOf(CredenciaisInvalidasException.class)
                    .hasMessage(ClientesService.CREDENCIAIS_INVALIDAS);
            assertThat(cliente.getPerfil()).isEqualTo(PerfilUsuario.DESATIVADO);
            then(repository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualização de senha")
    class AtualizacaoSenha {

        @Test
        void deveAlterarSenha_quandoNomeConferirESenhasCoincidirem() {

            Cliente cliente = clienteAtivo();
            given(repository.findById(1L)).willReturn(Optional.of(cliente));
            given(repository.save(cliente)).willReturn(cliente);
            ClienteAtualizaRequest request = atualizacao("Flor", "novaSenha1", "novaSenha1");

            ClienteAtualizaResponse resposta = service.atualizarSenha(1L, request);

            assertThat(cliente.getSenha()).isEqualTo("novaSenha1");
            assertThat(resposta.mensagem()).isEqualTo("Usuário: Flor senha alterada com sucesso!");
        }

        @Test
        void deveLancarCredenciaisInvalidas_quandoNomeForDiferente() {

            Cliente cliente = clienteAtivo();
            given(repository.findById(1L)).willReturn(Optional.of(cliente));
            ClienteAtualizaRequest request = atualizacao("Outro", "novaSenha1", "novaSenha1");

            assertThatThrownBy(() -> service.atualizarSenha(1L, request))
                    .isInstanceOf(CredenciaisInvalidasException.class)
                    .hasMessage("Nome está incorreto");
            assertThat(cliente.getSenha()).isEqualTo("senha123");
        }

        @Test
        void deveManterSenhaAntiga_quandoConfirmacaoForDiferente() {

            Cliente cliente = clienteAtivo();
            given(repository.findById(1L)).willReturn(Optional.of(cliente));
            ClienteAtualizaRequest request = atualizacao("Flor", "novaSenha1", "outraSenha");

            assertThatThrownBy(() -> service.atualizarSenha(1L, request))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("As senhas não coincidem");
            assertThat(cliente.getSenha()).isEqualTo("senha123");
            then(repository).should(never()).save(any());
        }

        @Test
        void deveLancarNaoEncontrado_quandoClienteNaoExistirNaAtualizacao() {

            given(repository.findById(5L)).willReturn(Optional.empty());
            ClienteAtualizaRequest request = atualizacao("Flor", "novaSenha1", "novaSenha1");

            assertThatThrownBy(() -> service.atualizarSenha(5L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Desativação")
    class Desativacao {

        @Test
        void deveDesativarPerfil_quandoClienteNaoTiverPedidos() {

            Cliente cliente = clienteAtivo();
            given(repository.findById(1L)).willReturn(Optional.of(cliente));
            given(repository.existsByIdAndPedidosIsNotEmpty(1L)).willReturn(false);

            service.desativarCliente(1L);

            assertThat(cliente.getPerfil()).isEqualTo(PerfilUsuario.DESATIVADO);
            then(repository).should().save(cliente);
        }

        @Test
        void deveManterPerfilAtivo_quandoClienteTiverPedidoEmAberto() {

            Cliente cliente = clienteAtivo();
            given(repository.findById(1L)).willReturn(Optional.of(cliente));
            given(repository.existsByIdAndPedidosIsNotEmpty(1L)).willReturn(true);

            assertThatThrownBy(() -> service.desativarCliente(1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("pedido de venda em aberto");
            assertThat(cliente.getPerfil()).isEqualTo(PerfilUsuario.CLIENTE);
            then(repository).should(never()).save(any());
        }

        @Test
        void deveLancarNaoEncontrado_quandoDesativarClienteInexistente() {

            given(repository.findById(5L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.desativarCliente(5L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuário com ID 5 não encontrado");
        }
    }

    private static ClienteCreatRequest cadastro(String senha, String confirmacao) {
        ClienteCreatRequest request = new ClienteCreatRequest();
        request.setNome("Flor");
        request.setTelefone(TELEFONE);
        request.setSenha(senha);
        request.setConfirmarSenha(confirmacao);
        return request;
    }

    private static ClienteLoginRequest login(String nome, String senha) {
        ClienteLoginRequest request = new ClienteLoginRequest();
        request.setNome(nome);
        request.setTelefone(TELEFONE);
        request.setSenha(senha);
        return request;
    }

    private static ClienteAtualizaRequest atualizacao(String nome, String senha, String confirmacao) {
        ClienteAtualizaRequest request = new ClienteAtualizaRequest();
        request.setNome(nome);
        request.setSenha(senha);
        request.setConfirmarSenha(confirmacao);
        return request;
    }

    private static Cliente clienteAtivo() {
        Cliente cliente = clienteDesativado();
        cliente.setPerfil(PerfilUsuario.CLIENTE);
        return cliente;
    }

    private static Cliente clienteDesativado() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Flor");
        cliente.setTelefone(TELEFONE);
        cliente.setSenha("senha123");
        cliente.setPerfil(PerfilUsuario.DESATIVADO);
        return cliente;
    }
}
