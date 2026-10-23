package com.autoCenterSilva.demo.controller;

import com.autocentersilva.demo.dto.request.cliente.ClienteAtualizaRequest;
import com.autocentersilva.demo.dto.request.cliente.ClienteCreatRequest;
import com.autocentersilva.demo.dto.request.cliente.ClienteLoginRequest;
import com.autocentersilva.demo.dto.response.cliente.*;
import com.autocentersilva.demo.entity.PerfilUsuario;
import com.autocentersilva.demo.service.ClientesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/clientes")
@CrossOrigin(origins = {"http://localhost:4200"})
public class ClientesController {

    private final ClientesService clientesService;

    public ClientesController(ClientesService clientesService) {
        this.clientesService = clientesService;
    }

    @PostMapping("/criar")
    public ResponseEntity<ClienteCreateResponse> salvar(@Valid @RequestBody ClienteCreatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientesService.salvarCliente(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ClienteLoginResponse> login(@Valid @RequestBody ClienteLoginRequest request) {
        return ResponseEntity.ok(clientesService.validarLogin(request));
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<ClienteAtualizaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteAtualizaRequest request) {
        return ResponseEntity.ok(clientesService.atualizarSenha(id, request));
    }

    @GetMapping("/busca/{id}")
    public ResponseEntity<ClienteBuscaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clientesService.findById(id));
    }

    @DeleteMapping("/desativar/{id}")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Long id) {
        clientesService.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<ClienteDisponiveisResponse>> buscarDisponiveis(
            @RequestParam(defaultValue = "CLIENTE") PerfilUsuario perfilUsuario) {
        return ResponseEntity.ok(listarPorPerfil(perfilUsuario));
    }

    @GetMapping("/usuarios/desativados")
    public ResponseEntity<List<ClienteDisponiveisResponse>> buscarDesativados(
            @RequestParam(defaultValue = "DESATIVADO") PerfilUsuario perfilUsuario) {
        return ResponseEntity.ok(listarPorPerfil(perfilUsuario));
    }

    private List<ClienteDisponiveisResponse> listarPorPerfil(PerfilUsuario perfilUsuario) {
        return clientesService.buscarDisponiveis(perfilUsuario).stream()
                .map(ClienteDisponiveisResponse::de)
                .toList();
    }
}
