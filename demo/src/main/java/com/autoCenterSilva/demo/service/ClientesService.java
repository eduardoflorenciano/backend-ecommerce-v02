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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientesService {
    static final String CREDENCIAIS_INVALIDAS = "Nome, telefone ou senha incorretos!";

    private static final Logger log = LoggerFactory.getLogger(ClientesService.class);

    private final ClientesRepository clientesRepository;

    public ClientesService(ClientesRepository clientesRepository) {
        this.clientesRepository = clientesRepository;
    }

    public List<Cliente> findAll() {
        return clientesRepository.findAll();
    }

    public ClienteBuscaResponse findById(Long id) {
        return ClienteBuscaResponse.de(buscarCliente(id));
    }

    @Transactional
    public ClienteCreateResponse salvarCliente(ClienteCreatRequest request) {
        validarSenhasIguais(request.getSenha(), request.getConfirmarSenha());
        if (clientesRepository.existsByNome(request.getNome())) {
            throw new RegraNegocioException("Nome de usuário inserido já existe! Não é possível criar o perfil!");
        }
        if (clientesRepository.existsByTelefone(request.getTelefone())) {
            throw new RegraNegocioException("Telefone inserido já existe! Não é possível criar o perfil!");
        }
        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setPerfil(PerfilUsuario.DESATIVADO);
        cliente.setSenha(request.getSenha());
        cliente.setTelefone(request.getTelefone());

        Cliente clienteSalvo = clientesRepository.save(cliente);
        log.info("Cliente {} cadastrado com sucesso", clienteSalvo.getId());
        return ClienteCreateResponse.de(clienteSalvo);
    }

    @Transactional
    public ClienteLoginResponse validarLogin(ClienteLoginRequest request) {
        Cliente cliente = clientesRepository.findByTelefone(request.getTelefone())
                .orElseThrow(() -> new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS));
        boolean credenciaisConferem = cliente.getSenha().equals(request.getSenha())
                && cliente.getNome().equals(request.getNome());
        if (!credenciaisConferem) {
            throw new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS);
        }
        cliente.setPerfil(PerfilUsuario.CLIENTE);
        clientesRepository.save(cliente);
        log.info("Login realizado pelo cliente {}", cliente.getId());
        return ClienteLoginResponse.de(cliente);
    }

    @Transactional
    public ClienteAtualizaResponse atualizarSenha(Long id, ClienteAtualizaRequest request) {
        Cliente cliente = buscarCliente(id);
        if (!cliente.getNome().equals(request.getNome())) {
            throw new CredenciaisInvalidasException("Nome está incorreto");
        }
        validarSenhasIguais(request.getSenha(), request.getConfirmarSenha());
        cliente.setSenha(request.getSenha());

        Cliente clienteAtualizado = clientesRepository.save(cliente);
        log.info("Senha do cliente {} atualizada", id);
        return ClienteAtualizaResponse.de(clienteAtualizado);
    }

    @Transactional
    public void desativarCliente(Long id) {
        Cliente cliente = buscarCliente(id);
        if (clientesRepository.existsByIdAndPedidosIsNotEmpty(id)) {
            throw new RegraNegocioException("Cliente está com um pedido de venda em aberto! Não é possível excluir!");
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    private static void validarSenhasIguais(String senha, String confirmacao) {
        if (!senha.equals(confirmacao)) {
            throw new RegraNegocioException("As senhas não coincidem");
        }
    }
}
