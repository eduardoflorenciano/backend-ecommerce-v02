
package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.cliente.ClienteAtualizaRequest;
import com.autoCenterSilva.demo.dto.request.cliente.ClienteCreatRequest;
import com.autoCenterSilva.demo.dto.request.cliente.ClienteLoginRequest;
import com.autoCenterSilva.demo.dto.response.cliente.ClienteAtualizaResponse;
import com.autoCenterSilva.demo.dto.response.cliente.ClienteBuscaResponse;
import com.autoCenterSilva.demo.dto.response.cliente.ClienteCreateResponse;
import com.autoCenterSilva.demo.dto.response.cliente.ClienteLoginResponse;
import com.autoCenterSilva.demo.entity.Cliente;
import com.autoCenterSilva.demo.entity.PerfilUsuario;
import com.autoCenterSilva.demo.entity.StatusPedido;
import com.autoCenterSilva.demo.exception.CredenciaisInvalidasException;
import com.autoCenterSilva.demo.exception.RegraNegocioException;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.ClientesRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientesService {

    static final String CREDENCIAIS_INVALIDAS = "Nome, telefone ou senha incorretos!";

    private static final Logger log = LoggerFactory.getLogger(ClientesService.class);

    private final ClientesRepository clientesRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientesService(ClientesRepository clientesRepository, PasswordEncoder passwordEncoder) {
        this.clientesRepository = clientesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Cliente> findAll() {
        return clientesRepository.findAll();
    }

    public ClienteBuscaResponse findById(Long id) {
        return ClienteBuscaResponse.de(buscarCliente(id));
    }

    public ClienteBuscaResponse findByIdDoUsuario(Long id, String telefone) {

        Cliente cliente = clientesRepository.findByIdAndTelefone(id, telefone)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado ou sem permissão para consulta"
                        )
                );

        return ClienteBuscaResponse.de(cliente);
    }

    @Transactional
    public ClienteCreateResponse salvarCliente(ClienteCreatRequest request) {

        validarSenhasIguais(
                request.getSenha(),
                request.getConfirmarSenha()
        );

        if (clientesRepository.existsByNome(request.getNome())) {
            throw new RegraNegocioException(
                    "Nome de usuário inserido já existe! Não é possível criar o perfil!"
            );
        }

        if (clientesRepository.existsByTelefone(request.getTelefone())) {
            throw new RegraNegocioException(
                    "Telefone inserido já existe! Não é possível criar o perfil!"
            );
        }

        Cliente cliente = new Cliente();

        cliente.setNome(request.getNome());
        cliente.setTelefone(request.getTelefone());

        cliente.setPerfil(PerfilUsuario.CLIENTE);

        cliente.setSenha(passwordEncoder.encode(request.getSenha()));

        Cliente clienteSalvo = clientesRepository.save(cliente);

        log.info("Cliente {} cadastrado com sucesso", clienteSalvo.getId());

        return ClienteCreateResponse.de(clienteSalvo);
    }

    @Transactional
    public ClienteLoginResponse validarLogin(ClienteLoginRequest request, String token) {
        Cliente cliente = clientesRepository.findByTelefone(request.getTelefone())
                .orElseThrow(() ->
                        new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS)
                );

        if (!cliente.getNome().equals(request.getNome())
                || cliente.getPerfil() == PerfilUsuario.DESATIVADO) {
            throw new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS);
        }

        log.info("Login realizado pelo usuário {}", cliente.getId());

        return ClienteLoginResponse.de(cliente, token);
    }


    @Transactional
    public ClienteAtualizaResponse atualizarSenha(Long id, ClienteAtualizaRequest request, String telefone) {
        Cliente cliente = clientesRepository.findByIdAndTelefone(id, telefone)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado ou sem permissão para alteração"
                        )
                );

        if (!cliente.getNome().equals(request.getNome())) {
            throw new CredenciaisInvalidasException("Nome está incorreto");
        }

        validarSenhasIguais(
                request.getSenha(),
                request.getConfirmarSenha()
        );

        cliente.setSenha(passwordEncoder.encode(request.getSenha()));

        Cliente clienteAtualizado = clientesRepository.save(cliente);

        log.info("Senha do cliente {} atualizada", id);

        return ClienteAtualizaResponse.de(clienteAtualizado);
    }


    @Transactional
    public void desativarCliente(Long id) {

        Cliente cliente = buscarCliente(id);

        if (clientesRepository.existsByIdAndPedidosStatus(
                id,
                StatusPedido.SOLICITADO
        )) {
            throw new RegraNegocioException(
                    "Cliente está com um pedido de venda em que Solicitado porem não foi efetuado pagamento! Não é possível excluir!"
            );
        }

        cliente.setPerfil(PerfilUsuario.DESATIVADO);

        clientesRepository.save(cliente);

        log.info("Cliente {} desativado com sucesso", id);
    }

    public List<Cliente> buscarDisponiveis(PerfilUsuario perfilUsuario) {
        return clientesRepository.findByPerfil(perfilUsuario);
    }

    private Cliente buscarCliente(Long id) {
        return clientesRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário com ID " + id + " não encontrado"
                        )
                );
    }

    private static void validarSenhasIguais(String senha, String confirmacao) {
        if (!senha.equals(confirmacao)) {
            throw new RegraNegocioException("As senhas não coincidem");
        }
    }
}
