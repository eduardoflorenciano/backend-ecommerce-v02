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
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.ClientesRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ClientesService {
    private final ClientesRepository clientesRepository;

    private static final Logger log = LoggerFactory.getLogger(ClientesService.class);

    public ClientesService(ClientesRepository clientesRepository) {
        this.clientesRepository = clientesRepository;
    }

    public List<Cliente> findAll() {
        return clientesRepository.findAll();
    }

    public ClienteBuscaResponse findById(Long id) {
        Cliente cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
        return ClienteBuscaResponse.de(cliente);
    }

    @Transactional
    public ClienteCreateResponse salvarCliente(ClienteCreatRequest clienteCreatRequest){
        if (!clienteCreatRequest.getSenha().equals(clienteCreatRequest.getConfirmarSenha())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }
        Cliente cliente = new Cliente();
        boolean existeUsuario = clientesRepository.existsByNome(clienteCreatRequest.getNome());
        boolean existeTelefone =  clientesRepository.existsByTelefone(clienteCreatRequest.getTelefone());
        if (existeUsuario) {
            throw new IllegalArgumentException("Nome de usuário inserido já existe! Não é possível criar o perfil!");
        }
        if (existeTelefone) {
            throw new IllegalArgumentException("Telefone inserido já existe! Não é possível criar o perfil!");
        }
        cliente.setNome(clienteCreatRequest.getNome());
        cliente.setPerfil(PerfilUsuario.DESATIVADO);
        cliente.setSenha( clienteCreatRequest.getSenha());
        cliente.setTelefone(clienteCreatRequest.getTelefone());

        Cliente clienteSalvo = this.clientesRepository.save(cliente);
        log.info("Cliente cadastrado com sucesso!");
        return ClienteCreateResponse.de(clienteSalvo);
    }

    @Transactional
    public ClienteLoginResponse validarLogin(ClienteLoginRequest clienteLoginRequest) {
        Cliente cliente = this.clientesRepository.findByTelefone(clienteLoginRequest.getTelefone())
                .orElseThrow(() -> new IllegalArgumentException("Telefone informado está incorreto!"));
        if (!cliente.getSenha().equals(clienteLoginRequest.getSenha())){
            throw new IllegalArgumentException("Senha informada está incorreto!");
        }
        if (!cliente.getNome().equals(clienteLoginRequest.getNome())){
            throw new IllegalArgumentException("Nome informado está incorreto!");
        }
        cliente.setPerfil(PerfilUsuario.CLIENTE);
        clientesRepository.save(cliente);
        log.info("Login realizado com sucesso! {}", clienteLoginRequest.getNome());
        return ClienteLoginResponse.de(cliente);
    }

    @Transactional
    public ClienteAtualizaResponse atualizarSenha(Long id, ClienteAtualizaRequest clienteAtualizaRequest){
        Cliente cliente = clientesRepository.findById((id)).orElseThrow(()  ->  new RuntimeException("Cliente não encontrado"));

        if (!cliente.getNome().equals(clienteAtualizaRequest.getNome())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nome está incorreto");
        }
        cliente.setSenha(clienteAtualizaRequest.getSenha());

        Cliente clienteAtualizado = this.clientesRepository.save(cliente);
        log.info("Senha do usuário {} atualizado  com sucesso!", clienteAtualizado.getNome());
        return ClienteAtualizaResponse.de(clienteAtualizado);
    }

    public void desativarCliente(Long id){
        Cliente cliente = clientesRepository.findById((id)).orElseThrow(()  ->  new RuntimeException("Cliente não encontrado"));
        boolean existePedidoDeCompra = clientesRepository.existsByIdAndPedidosIsNotEmpty(id);
        if (existePedidoDeCompra) {
            throw new IllegalArgumentException("Cliente está com um pedido de venda em aberto! Não é possível excluir!");

        }
        cliente.setPerfil(PerfilUsuario.DESATIVADO);
        clientesRepository.save(cliente);
        log.info("Cliente com o ID {} desativado com sucesso!", id);
    }

    public List<Cliente> buscarDisponiveis(PerfilUsuario perfilUsuario){
        log.info("Verificando o status do perfil");
        return clientesRepository.findByPerfil(perfilUsuario);
    }


}
