
package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.cliente.ClienteAtualizaRequest;
import com.autoCenterSilva.demo.dto.request.cliente.ClienteCreatRequest;
import com.autoCenterSilva.demo.dto.request.cliente.ClienteLoginRequest;
import com.autoCenterSilva.demo.dto.response.cliente.*;
import com.autoCenterSilva.demo.entity.PerfilUsuario;
import com.autoCenterSilva.demo.security.CustomUserDetailsService;
import com.autoCenterSilva.demo.security.JwtService;
import com.autoCenterSilva.demo.service.ClientesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClientesController {

    private final ClientesService clientesService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public ClientesController(
            ClientesService clientesService,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.clientesService = clientesService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/criar")
    public ResponseEntity<ClienteCreateResponse> salvar(
            @Valid @RequestBody ClienteCreatRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientesService.salvarCliente(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ClienteLoginResponse> login(
            @Valid @RequestBody ClienteLoginRequest request
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getTelefone(),
                        request.getSenha()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getTelefone());

        String token = jwtService.gerarToken(userDetails);

        return ResponseEntity.ok(
                clientesService.validarLogin(request, token)
        );
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<ClienteAtualizaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteAtualizaRequest request, Authentication authentication) {
        return ResponseEntity.ok(
                clientesService.atualizarSenha(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/busca/{id}")
    public ResponseEntity<ClienteBuscaResponse> buscar(@PathVariable Long id, Authentication authentication) {

        if (ehAdm(authentication)) {
            return ResponseEntity.ok(clientesService.findById(id));
        }

        return ResponseEntity.ok(
                clientesService.findByIdDoUsuario(
                        id,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/desativar/{id}")
    public ResponseEntity<Void> desativarUsuario(
            @PathVariable Long id,
            Authentication authentication
    ) {

        if (!ehAdm(authentication)) {
            clientesService.findByIdDoUsuario(id, authentication.getName());
        }

        clientesService.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    private boolean ehAdm(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADM")
                );
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<ClienteDisponiveisResponse>> buscarDisponiveis(@RequestParam(defaultValue = "CLIENTE") PerfilUsuario perfilUsuario) {
        return ResponseEntity.ok(listarPorPerfil(perfilUsuario));
    }

    @GetMapping("/usuarios/desativados")
    public ResponseEntity<List<ClienteDisponiveisResponse>> buscarDesativados(
            @RequestParam(defaultValue = "DESATIVADO") PerfilUsuario perfilUsuario
    ) {
        return ResponseEntity.ok(listarPorPerfil(perfilUsuario));
    }

    private List<ClienteDisponiveisResponse> listarPorPerfil(
            PerfilUsuario perfilUsuario
    ) {
        return clientesService.buscarDisponiveis(perfilUsuario)
                .stream()
                .map(ClienteDisponiveisResponse::de)
                .toList();
    }
}
