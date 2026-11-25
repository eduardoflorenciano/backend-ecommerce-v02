package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.pedido.PedidoCreateRequest;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autoCenterSilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoAdminResponse;
import com.autoCenterSilva.demo.dto.request.pedido.PedidoCancelarRequest;
import com.autoCenterSilva.demo.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/pedidos")
public class PedidosController {

    private final PedidoService pedidoService;

    public PedidosController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/criar")
    public ResponseEntity<PedidoCreateResponse> salvarPedido(
            @Valid @RequestBody PedidoCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.salvar(authentication.getName(), request));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<PedidosListagemResponse>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.buscarPedidos());
    }

    @GetMapping("/meus")
    public ResponseEntity<List<PedidoAdminResponse>> listarMeusPedidos(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                pedidoService.buscarPedidosDoUsuario(authentication.getName())
        );
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelarPedido(
            @PathVariable Long id,
            @Valid @RequestBody PedidoCancelarRequest request,
            Authentication authentication
    ) {

        boolean equipe = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADM")
                                || authority.getAuthority().equals("ROLE_ATENDENTE")
                );

        boolean cancelado = pedidoService.cancelarPedido(
                id, request, authentication.getName(), equipe
        );

        if (!cancelado) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("O prazo da reserva expirou. O pedido foi expirado e o estoque liberado");
        }

        return ResponseEntity.ok(
                "Pedido cancelado com sucesso. O estoque reservado foi liberado"
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<List<PedidoAdminResponse>> listarPedidosAdmin() {
        return ResponseEntity.ok(pedidoService.buscarPedidosAdmin());
    }

    @PutMapping("/{id}/confirmar-pagamento")
    public ResponseEntity<String> confirmarPagamento(@PathVariable Long id) {

        boolean confirmado = pedidoService.confirmarPagamento(id);

        if (!confirmado) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("O prazo da reserva expirou. O pedido foi expirado e o estoque liberado");
        }

        return ResponseEntity.ok(
                "Pagamento confirmado! Pedido finalizado com sucesso"
        );
    }

}
