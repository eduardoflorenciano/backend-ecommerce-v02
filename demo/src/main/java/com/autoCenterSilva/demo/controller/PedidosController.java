package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.PedidoCreateRequest;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autoCenterSilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autoCenterSilva.demo.service.PedidoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/pedidos")
public class PedidosController {
    private static final Logger log = LoggerFactory.getLogger(PedidosController.class);

    @Autowired
    private PedidoService pedidosService;

    @PostMapping("/criar")
    public ResponseEntity<PedidoCreateResponse> salvarPedido(@Valid @RequestBody PedidoCreateRequest pedidoCreateRequest){
        log.info("Requisição recebida: POST -  /api/pedidos/criar/{}",pedidoCreateRequest);
        PedidoCreateResponse response = this.pedidosService.salvar(pedidoCreateRequest);
        return new  ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<PedidosListagemResponse>> listarPedidos(){
        log.info("Requisição recebida: GET -  /api/pedidos/listar");
        List<PedidosListagemResponse> pedidos = pedidosService.buscarPedidos();
        return new  ResponseEntity<>(pedidos, HttpStatus.OK);
    }
}
