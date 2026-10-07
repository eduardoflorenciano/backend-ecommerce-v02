package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.cliente.ClienteAtualizaRequest;
import com.autoCenterSilva.demo.dto.request.cliente.ClienteCreatRequest;
import com.autoCenterSilva.demo.dto.request.cliente.ClienteLoginRequest;
import com.autoCenterSilva.demo.dto.response.cliente.*;
import com.autoCenterSilva.demo.entity.PerfilUsuario;
import com.autoCenterSilva.demo.service.ClientesService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/clientes")
@CrossOrigin(origins = {"http://localhost:4200"})
public class ClientesController {
    private static final Logger log = LoggerFactory.getLogger(ClientesController.class);

    @Autowired
    private ClientesService clientesService;

    @PostMapping("/criar")
    public ResponseEntity<ClienteCreateResponse> salvar(@Valid @RequestBody ClienteCreatRequest clienteCreatRequest){
        log.info("Requisição recebida: POST -  /api/cliente/criar {}", clienteCreatRequest);
        ClienteCreateResponse response = this.clientesService.salvarCliente(clienteCreatRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ClienteLoginResponse> login(@Valid @RequestBody ClienteLoginRequest clienteLoginRequest){
        log.info("Requisição recebida: POST -  /api/cliente/login {}", clienteLoginRequest);
        ClienteLoginResponse clienteLogin = this.clientesService.validarLogin(clienteLoginRequest);
        log.info("Login valido: {}", clienteLogin);
        return new ResponseEntity<>(clienteLogin, HttpStatus.OK);
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<ClienteAtualizaResponse> atualizar(@Valid @PathVariable Long id, @RequestBody ClienteAtualizaRequest clienteAtualizRequest){
        log.info("Requisição recebida: PATCH -  /api/cliente/atualizar/{} {}",id, clienteAtualizRequest);
        ClienteAtualizaResponse response = this.clientesService.atualizarSenha(id, clienteAtualizRequest);
        log.info("Usuario atualizado: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/busca/{id}")
    public ResponseEntity<ClienteBuscaResponse>Busca(@Valid @PathVariable Long id){
        log.info("Requisição recebida: GET -  /api/cliente/busca/{}",id);
        ClienteBuscaResponse response = this.clientesService.findById(id);
        log.info("Busca valida: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/desativar/{id}")
    public ResponseEntity<String> desativarUsuario(@Valid @PathVariable Long id){
        log.info("Requisição recebida: DELETE -  /api/cliente/desativar/{}",id);
        clientesService.desativarCliente(id);
        log.info("Usuario desativado: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<ClienteDisponiveisResponse>>buscaDisponveis(@RequestParam(defaultValue = "CLIENTE")PerfilUsuario perfilUsuario){
        log.info("Requisição recebida: GET -  /api/cliente/usuarios{}",perfilUsuario);
        List<ClienteDisponiveisResponse> disponiveis  = this.clientesService.buscarDisponiveis(perfilUsuario)
                .stream()
                .map(ClienteDisponiveisResponse::de)
                .toList();
        log.info("ClientesDisponiveis: {}", disponiveis);
        return new ResponseEntity<>(disponiveis, HttpStatus.OK);
    }

    @GetMapping("/usuarios/desativados")
    public ResponseEntity<List<ClienteDisponiveisResponse>>buscaDesativados(@RequestParam(defaultValue = "DESATIVADO")PerfilUsuario perfilUsuario){
        log.info("Requisição recebida: GET -  /api/cliente/usuarios/desativados");
        List<ClienteDisponiveisResponse> indisponiveis  = this.clientesService.buscarDisponiveis(perfilUsuario)
                .stream()
                .map(ClienteDisponiveisResponse::de)
                .toList();
        log.info("ClientesIndisponiveis: {}", indisponiveis);
        return new ResponseEntity<>(indisponiveis, HttpStatus.OK);
    }
}
